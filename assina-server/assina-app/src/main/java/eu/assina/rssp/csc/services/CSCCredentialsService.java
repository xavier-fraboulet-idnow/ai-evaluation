/*
 Copyright 2024 European Commission

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      https://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package eu.assina.rssp.csc.services;

import eu.assina.csc.payload.*;
import eu.assina.rssp.api.model.User;
import eu.assina.rssp.api.model.UserOID4VP;
import eu.assina.rssp.api.services.UserService;
import eu.assina.rssp.common.error.AssinaError;
import eu.assina.rssp.common.model.AssinaCredential;
import eu.assina.rssp.api.services.CredentialService;
import eu.assina.rssp.common.config.PaginationHelper;
import eu.assina.rssp.common.error.ApiException;
import eu.assina.rssp.crypto.AssinaCryptoService;
import eu.assina.csc.error.CSCInvalidRequest;
import eu.assina.csc.model.AssinaCSCConstants;
import eu.assina.csc.model.CertificateStatus;
import eu.assina.rssp.security.UserPrincipal;
import eu.assina.rssp.security.oauth2.AssinaOID4VPUserService;
import eu.assina.rssp.security.oauth2.OID4VPVerifierClient;
import eu.assina.rssp.util.CertificateUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CSCCredentialsService {

	// Allowed values for the certificates attribute: not used in payload but derived in validation
	public enum CertificatesRequest { none, single, chain }

	@Autowired
	PaginationHelper paginationHelper;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private OID4VPVerifierClient verifierClient;

	@Autowired
	AssinaOID4VPUserService userOID4VPService;

	private CredentialService credentialService;
	private UserService userService;
	private AssinaCryptoService cryptoService;
	private CSCSADProvider sadProvider;


	public CSCCredentialsService(CredentialService credentialService,
								 UserService userService,
								 AssinaCryptoService cryptoService,
								 CSCSADProvider sadProvider) {
		this.credentialService = credentialService;
		this.userService = userService;
		this.cryptoService = cryptoService;
		this.sadProvider = sadProvider;
	}

	/**
	 * Returns a list of credentials and a pageToken to allow the client to get the next page.
	 *
	 * Page start is taken from the pageToken in the request, and page length is calculated as
	 * the maximum of the MaxResults in the request and the max page size defined in
	 * application.yml under csc.api, or the default pageSize if the request does nto specify
	 */
	public CSCCredentialsListResponse listCredentials(CSCCredentialsListRequest listRequest) {
		Pageable pageable;
		String nextPageToken;
		try {
			pageable = paginationHelper.pageTokenToPageable(listRequest.getPageToken(), listRequest.getMaxResults());
			nextPageToken = paginationHelper.pageableToNextPageToken(pageable);
		}
		catch (Exception e) {
			// invalid page token per the CSC spec
			throw new ApiException(CSCInvalidRequest.InvalidPageToken, e);
		}

		final Page<AssinaCredential> credentialsPage =
				credentialService.getCredentialsByOwner(listRequest.getUserId(), pageable);

		List<CredentialInfo> credentialAliases = new ArrayList<>();	
		for(AssinaCredential ac: credentialsPage){
			CredentialInfo ci = new CredentialInfo(ac.getAlias(), ac.getIssuerDN(), ac.getSubjectDN(), ac.getValidFrom(), ac.getValidTo());
			credentialAliases.add(ci);
		}

		// final List<String> credentialAliases = credentialsPage.map(AssinaCredential::getAlias).stream().collect(Collectors.toList());

		CSCCredentialsListResponse response = new CSCCredentialsListResponse();
		response.setCredentialInfo(credentialAliases);
		// don't set the next page token if its the last page
		if (credentialsPage.isLast()) {
			nextPageToken = null;
		}
		response.setNextPageToken(nextPageToken);
		return response;
	}

	/**
	 * Returns details about a the requested credential.
	 */
	public CSCCredentialsInfoResponse getCredentialsInfo(CSCCredentialsInfoRequest infoRequest) {

		final String credentialID = infoRequest.getCredentialID();
		final AssinaCredential credential = loadCredential(credentialID);


		CSCCredentialsInfoResponse response = new CSCCredentialsInfoResponse();

		// One of implicit | explicit | oauth2code
		response.setAuthMode(AssinaCSCConstants.CSC_AUTH_MODE);
		response.setDescription(credential.getDescription());
		// this value matches that in credential/authorize
		response.setMultisign(AssinaCSCConstants.CSC_MAX_REQUEST_SIGNATURES);
		// “1”: The hash  to-be-signed is not linked to the signature activation data.
		response.setSCAL(AssinaCSCConstants.CSC_SCAL);
		response.setKey(buildKeyInfo(credential));

		CSCCredentialsInfoResponse.Cert cert = new CSCCredentialsInfoResponse.Cert();
		final String pemCertificate = credential.getCertificate();
		final X509Certificate x509Certificate = cryptoService.pemToX509Certificate(pemCertificate);
		List<String> cscCertificates = new ArrayList<>();
		switch (toCertsRequest(infoRequest.getCertificates())) {
			case none:
				// nothing requested, move on
				break;
			case single:
			    // certificates are already stored as PEM strings which are Base64 encoded
				cscCertificates.add(pemCertificate);
				break;
			case chain:
				// TODO consider supporting chain request
				throw new IllegalArgumentException("Not Yet Implmented");
		}
		cert.setCertificates(cscCertificates);

		// only if certInfo is true in the request:
		if (infoRequest.isCertInfo()) {
			addCertInfo(cert, x509Certificate);
		}
		response.setCert(cert);
        if (cryptoService.isCertificateExpired(x509Certificate)) {
			cert.setStatus(CertificateStatus.expired.name());
		}
        else {
			// Consider handling other cases like "revoked" and "suspended"
			cert.setStatus(CertificateStatus.valid.name());
		}

		// Per CSC spec, we only return OTP and PIN info if authInfo is true in the request
		if (infoRequest.isAuthInfo()) {
			response.setPIN(buildPINInfo());
			response.setOTP(buildOTPInfo());
		}

		return response;
	}

	public CSCCredentialsInfoResponse getCredentialsInfoFromAlias(UserPrincipal userPrincipal, CSCCredentialsInfoRequest infoRequest) {

		final String credentialAlias = infoRequest.getCredentialID();
		final AssinaCredential credential = loadCredential(userPrincipal.getId(), credentialAlias);


		CSCCredentialsInfoResponse response = new CSCCredentialsInfoResponse();

		// One of implicit | explicit | oauth2code
		response.setAuthMode(AssinaCSCConstants.CSC_AUTH_MODE);
		response.setDescription(credential.getDescription());
		// this value matches that in credential/authorize
		response.setMultisign(AssinaCSCConstants.CSC_MAX_REQUEST_SIGNATURES);
		// “1”: The hash  to-be-signed is not linked to the signature activation data.
		response.setSCAL(AssinaCSCConstants.CSC_SCAL);
		response.setKey(buildKeyInfo(credential));

		CSCCredentialsInfoResponse.Cert cert = new CSCCredentialsInfoResponse.Cert();
		final String pemCertificate = credential.getCertificate();
		final X509Certificate x509Certificate = cryptoService.pemToX509Certificate(pemCertificate);
		List<String> cscCertificates = new ArrayList<>();
		switch (toCertsRequest(infoRequest.getCertificates())) {
			case none:
				// nothing requested, move on
				break;
			case single:
			    // certificates are already stored as PEM strings which are Base64 encoded
				cscCertificates.add(pemCertificate);
				break;
			case chain:
				// TODO consider supporting chain request
				throw new IllegalArgumentException("Not Yet Implmented");
		}
		cert.setCertificates(cscCertificates);

		// only if certInfo is true in the request:
		if (infoRequest.isCertInfo()) {
			addCertInfo(cert, x509Certificate);
		}
		response.setCert(cert);
        if (cryptoService.isCertificateExpired(x509Certificate)) {
			cert.setStatus(CertificateStatus.expired.name());
		}
        else {
			// Consider handling other cases like "revoked" and "suspended"
			cert.setStatus(CertificateStatus.valid.name());
		}

		// Per CSC spec, we only return OTP and PIN info if authInfo is true in the request
		if (infoRequest.isAuthInfo()) {
			response.setPIN(buildPINInfo());
			response.setOTP(buildOTPInfo());
		}

		return response;
	}

	/** helper to convert the string certificates property to an enum */
	private CertificatesRequest toCertsRequest(String certificates) {
		if (StringUtils.hasText(certificates)) {
			try {
				return CertificatesRequest.valueOf(certificates);
			} catch (IllegalArgumentException e) {
				// certificates was not one of none, single or chain, which is an error
				throw new ApiException(CSCInvalidRequest.InvalidCertificatesParameter);
			}
		}
		else{
			// certificates is optional and defaults to single
			 return CertificatesRequest.single;
	}
	}

	/**
	 * Update info about the cert in the response
	 * According to the CSC standard, these properties are only set when the certInfo property
	 * is true in the request
	 */
	private void addCertInfo(CSCCredentialsInfoResponse.Cert cert, X509Certificate x509Certificate) {
		cert.setIssuerDN(x509Certificate.getIssuerDN().getName());
		cert.setSubjectDN(x509Certificate.getSubjectDN().getName());
		cert.setSerialNumber(String.valueOf(x509Certificate.getSerialNumber()));

		// per CSC spec: encoded as GeneralizedTime (RFC 5280 [8]) e.g.  “YYYYMMDDHHMMSSZ”
		cert.setValidFrom(CertificateUtils.x509Date(x509Certificate.getNotBefore()));
		cert.setValidTo(CertificateUtils.x509Date(x509Certificate.getNotAfter()));

	}

	private CSCCredentialsInfoResponse.OTP buildOTPInfo() {
		return null; // later we might add OTP support
	}

	private CSCCredentialsInfoResponse.PIN buildPINInfo() {
		CSCCredentialsInfoResponse.PIN pinInfo = new CSCCredentialsInfoResponse.PIN();

		// presence is true|false|optional
		// ASSINA: we are using PIN so true
		pinInfo.setPresence(Boolean.TRUE.toString());
		// PIN  is numeric (use "A" for alpha, "N" for numeric only)
		pinInfo.setLabel("PIN");
		pinInfo.setDescription("PIN required for authorizing Assina to sign with this credential");
		pinInfo.setFormat("N");
		return pinInfo;
	}

	private CSCCredentialsInfoResponse.Key buildKeyInfo(AssinaCredential credential) {
		CSCCredentialsInfoResponse.Key key = new CSCCredentialsInfoResponse.Key();
		key.setAlgo(credential.getKeyAlgorithmOIDs());
		key.setCurve(credential.getECDSACurveOID());
		key.setLen(String.valueOf(credential.getKeyBitLength())); // num bits in key
        key.setStatus(credential.isKeyEnabled() ? "enabled" : "disabled");
        return key;
	}

	protected AssinaCredential loadCredential(String owner, String credentialAlias) {
		final AssinaCredential credential =
				credentialService.getCredentialWithAlias(owner, credentialAlias).orElseThrow(
						() -> new ApiException(CSCInvalidRequest.InvalidCredentialId,
								"No credential found with the given Id", credentialAlias));
		return credential;
	}

	protected AssinaCredential loadCredential(String credentialID) {
		final AssinaCredential credential =
				credentialService.getCredentialWithId(credentialID).orElseThrow(
						() -> new ApiException(CSCInvalidRequest.InvalidCredentialId,
								"No credential found with the given Id", credentialID));
		return credential;
	}


	/**
	 * Valdiate the PIN provioded and generate a SAD token for the user to authorize the credentials.
	 *
	 * @param userPrincipal user making the request - must own the credentials
	 * @param authorizeRequest authorization request
	 */
	public CSCCredentialsAuthorizeResponse authorizeCredential (UserPrincipal userPrincipal,
																CSCCredentialsAuthorizeRequest authorizeRequest,
																String nonce,
																String presentation_id) throws Exception {

		CSCCredentialsAuthorizeResponse response = new CSCCredentialsAuthorizeResponse();
		String id = userPrincipal.getId();
		Optional<User> optionalUser = userService.getUserById(id);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			response = authorizeCredentialWithPin(user, authorizeRequest, response);
		} else {
			Optional<UserOID4VP> optionalUserOID4VP = userService.getUserOID4VPById(id);
			if (optionalUserOID4VP.isPresent()) {
				UserOID4VP user = optionalUserOID4VP.get();
				response = authorizeCredentialWithOID4VP(user, authorizeRequest, response, nonce, presentation_id);
			} else {
				throw new ApiException(AssinaError.UserNotFound, "Failed to find user {}", id);
			}
		}
		return response;
	}

	public CSCCredentialsAuthorizeResponse authorizeCredentialWithOID4VP (UserOID4VP user,
																		  CSCCredentialsAuthorizeRequest authorizeRequest,
																		  CSCCredentialsAuthorizeResponse response,
																		  String nonce,
																		  String presentation_id) throws Exception {

		System.out.println("Call function authorizeCredentialWithOID4VP.");

		final String credentialID = authorizeRequest.getCredentialID();
		// final AssinaCredential credential = loadCredential(user.getId(), credentialID);
		// Await Response from Verifier
		String message = verifierClient.getVPTokenFromVerifier(nonce, presentation_id);
		// System.out.println(message);

		JSONObject jobj = new JSONObject(message);
		String vp_token = jobj.getString("vp_token");
		UserOID4VP loaded = userOID4VPService.loadUserOID4VPFromCBOR(vp_token).getUser();

		if (!Objects.equals(loaded.getHash(), user.getHash())) {
			throw new ApiException(CSCInvalidRequest.HashNotAuthorizedBySAD,
					"The access to the Credentials was not authorized.");
		}

		// se user == userResponse => allow use credential?
		String SAD = sadProvider.createSAD(credentialID);
		final long lifetimeSeconds = sadProvider.getLifetimeSeconds();
		response.setSAD(SAD);
		response.setExpiresIn(lifetimeSeconds - 1); // subtract a second to be sure

		return response;
	}

	public CSCCredentialsAuthorizeResponse authorizeCredentialWithPin(User user, CSCCredentialsAuthorizeRequest authorizeRequest, CSCCredentialsAuthorizeResponse response) throws Exception{
		final String credentialID = authorizeRequest.getCredentialID();
		// final AssinaCredential credential = loadCredential(user.getId(), credentialID);
		final String requestPIN = authorizeRequest.getPIN();
		final String userPIN = user.getEncodedPIN();
		if (passwordEncoder.matches(requestPIN, userPIN)) {
			// encoded request pin is valid for this user, so proceed
			// we don't really need the credential ID in the SAD, consider
			// in the future supporting SCAL=2 and storing the hash and validating later
			String SAD = sadProvider.createSAD(credentialID);
			final long lifetimeSeconds = sadProvider.getLifetimeSeconds();
			response.setSAD(SAD);
			response.setExpiresIn(lifetimeSeconds - 1); // subtract a second to be sure
		}
		else {
			throw new ApiException(CSCInvalidRequest.InvalidPin,
					"The provided PIN does not match the one created for this user");
		}
		return response;
	}


	public RedirectLinkResponse authorizationLinkCredential (UserPrincipal userPrincipal) throws Exception {
		RedirectLinkResponse response;
		String id = userPrincipal.getId();

		Optional<UserOID4VP> optionalUserOID4VP = userService.getUserOID4VPById(id);
		if (optionalUserOID4VP.isPresent()) {
            response = verifierClient.initPresentationTransaction();
		} else {
			throw new ApiException(AssinaError.UserNotFound, "Failed to find user {}", id);
		}
		return response;
	}
}
