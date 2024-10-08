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

package eu.europa.ec.eudi.signer.rssp.csc.services;

import eu.europa.ec.eudi.signer.csc.error.CSCInvalidRequest;
import eu.europa.ec.eudi.signer.csc.payload.CSCSignaturesSignHashRequest;
import eu.europa.ec.eudi.signer.csc.payload.CSCSignaturesSignHashResponse;
import eu.europa.ec.eudi.signer.rssp.api.model.LoggerUtil;
import eu.europa.ec.eudi.signer.rssp.api.services.CredentialService;
import eu.europa.ec.eudi.signer.rssp.api.services.UserService;
import eu.europa.ec.eudi.signer.rssp.common.config.AuthProperties;
import eu.europa.ec.eudi.signer.rssp.common.error.ApiException;
import eu.europa.ec.eudi.signer.rssp.common.error.SignerError;
import eu.europa.ec.eudi.signer.rssp.crypto.CryptoService;
import eu.europa.ec.eudi.signer.rssp.entities.Credential;
import eu.europa.ec.eudi.signer.rssp.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


@Service
public class CodiumRefacoCSCSignaturesService {

	private static final Logger log = LoggerFactory.getLogger(CodiumRefacoCSCSignaturesService.class);
	private final CredentialService credentialService;
	private final UserService userService;
	private final CryptoService cryptoService;
	private final CSCSADProvider sadProvider;
	private final AuthProperties authProperties;

	public CodiumRefacoCSCSignaturesService(CredentialService credentialService, UserService userService,
											CryptoService cryptoService, CSCSADProvider sadProvider, AuthProperties authProperties) {
		this.credentialService = credentialService;
		this.userService = userService;
		this.cryptoService = cryptoService;
		this.sadProvider = sadProvider;
		this.authProperties = authProperties;
	}

	/**
	 * Sign the provided hash with the credential specified in the request
	 *
	 * @param userPrincipal
	 * @param signHashRequest
	 * @return
	 */
	public CSCSignaturesSignHashResponse signHash(UserPrincipal userPrincipal,
												  @Valid @RequestBody CSCSignaturesSignHashRequest signHashRequest) {

		validateUser(userPrincipal);

		Credential credential = getCredential(userPrincipal, signHashRequest.getCredentialID());

		validateSAD(signHashRequest.getSAD(), userPrincipal);

		List<String> signedHashes = signHashes(signHashRequest, credential);

		return createResponse(signedHashes, signHashRequest.getClientData(), userPrincipal);
	}

	private void validateUser(UserPrincipal userPrincipal) {
		if (userService.getUserById(userPrincipal.getId()).isEmpty()) {
			log.error("{}: User not found.", SignerError.UserNotFound.getCode());
			throw new ApiException(SignerError.UserNotFound, "User not found.");
		}
	}

	private Credential getCredential(UserPrincipal userPrincipal, String credentialAlias) {
		return credentialService.getCredentialWithAlias(userPrincipal.getId(), credentialAlias)
				.orElseThrow(() -> {
					logUserAction(userPrincipal.getId(), 0, "");
					return new ApiException(CSCInvalidRequest.InvalidCredentialId,
							"No credential found with the given Id", credentialAlias);
				});
	}

	private void validateSAD(String sad, UserPrincipal userPrincipal) {
		try {
			sadProvider.validateSAD(sad);
		} catch (Exception e) {
			log.error("{}: SAD not validated.", SignerError.FailedToValidateSAD.getCode());
			logUserAction(userPrincipal.getId(), 0, "");
			throw new ApiException(SignerError.FailedToValidateSAD);
		}
	}

	private List<String> signHashes(CSCSignaturesSignHashRequest signHashRequest, Credential credential) {
		List<String> signedHashes = new ArrayList<>();
		for (String hash : signHashRequest.getHash()) {
			try {
				String signedData = cryptoService.signWithPemCertificate(
						hash,
						credential.getCertificate(),
						credential.getCertificateChains(),
						credential.getPrivateKeyHSM(),
						signHashRequest.getSignAlgo(),
						signHashRequest.getSignAlgoParams());
				signedHashes.add(signedData);
			} catch (Exception e) {
				log.error("{}: Failed to sign.", SignerError.FailedSigningData.getCode());
				logUserAction(signHashRequest.getCredentialID(), 0, "");
				throw new ApiException(SignerError.FailedSigningData, e);
			}
		}
		return signedHashes;
	}

	private CSCSignaturesSignHashResponse createResponse(List<String> signedHashes, String pdfName, UserPrincipal userPrincipal) {
		CSCSignaturesSignHashResponse response = new CSCSignaturesSignHashResponse();
		response.setSignatures(signedHashes);
		String logDescription = "CMS Signed Data Bytes: " + signedHashes + " | File Name: " + pdfName;
		logUserAction(userPrincipal.getId(), 1, logDescription);
		return response;
	}

	private void logUserAction(String userId, int actionType, String description) {
		LoggerUtil.logsUser(authProperties.getDatasourceUsername(),
				authProperties.getDatasourcePassword(), actionType, userId, 6, description);
		LoggerUtil.desc = "";
	}
}