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
import eu.europa.ec.eudi.signer.rssp.entities.User;
import eu.europa.ec.eudi.signer.rssp.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


@Service
public class CodiumRefacoCSCComplexPromptSignaturesService {

	private static final Logger log = LoggerFactory.getLogger(CodiumRefacoCSCComplexPromptSignaturesService.class);
	private final CredentialService credentialService;
	private final UserService userService;
	private final CryptoService cryptoService;
	private final CSCSADProvider sadProvider;
	private final AuthProperties authProperties;

	public CodiumRefacoCSCComplexPromptSignaturesService(CredentialService credentialService, UserService userService,
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

		CSCSignaturesSignHashResponse response = new CSCSignaturesSignHashResponse();
		String pdfName = signHashRequest.getClientData();
		final String credentialAlias = signHashRequest.getCredentialID();
		final String sad = signHashRequest.getSAD();

		User user = userService.getUserById(userPrincipal.getId())
				.orElseThrow(() -> new ApiException(SignerError.UserNotFound, "User not found."));

		Credential credential = credentialService.getCredentialWithAlias(userPrincipal.getId(), credentialAlias)
				.orElseThrow(() -> {
					logUserAction(userPrincipal.getId(), 0, "No credential found with the given Id");
					return new ApiException(CSCInvalidRequest.InvalidCredentialId,
							"No credential found with the given Id", credentialAlias);
				});

		validateSAD(sad, userPrincipal.getId());

		List<String> signedHashes = signHashes(signHashRequest, credential);
		response.setSignatures(signedHashes);

		logUserAction(userPrincipal.getId(), 1, "CMS Signed Data Bytes: " + signedHashes + " | File Name: " + pdfName);
		return response;
	}

	private void validateSAD(String sad, String userId) {
		try {
			sadProvider.validateSAD(sad);
		} catch (Exception e) {
			logUserAction(userId, 0, "SAD not validated.");
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
				logUserAction(credential.getOwner(), 0, "Failed to sign.");
				throw new ApiException(SignerError.FailedSigningData, e);
			}
		}
		return signedHashes;
	}

	private void logUserAction(String userId, int success, String info) {
		LoggerUtil.logsUser(authProperties.getDatasourceUsername(),
				authProperties.getDatasourcePassword(), success, userId, 6, info);
	}
}