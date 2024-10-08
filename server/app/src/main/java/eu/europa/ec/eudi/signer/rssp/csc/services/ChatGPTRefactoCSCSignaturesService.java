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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatGPTRefactoCSCSignaturesService {
	private static final Logger log = LoggerFactory.getLogger(CSCSignaturesService.class);

	private final CredentialService credentialService;
	private final UserService userService;
	private final CryptoService cryptoService;
	private final CSCSADProvider sadProvider;
	private final AuthProperties authProperties;

	public ChatGPTRefactoCSCSignaturesService(CredentialService credentialService, UserService userService,
								CryptoService cryptoService, CSCSADProvider sadProvider, AuthProperties authProperties) {
		this.credentialService = credentialService;
		this.userService = userService;
		this.cryptoService = cryptoService;
		this.sadProvider = sadProvider;
		this.authProperties = authProperties;
	}

	/**
	 * Sign the provided hash with the credential specified in the request.
	 *
	 * @param userPrincipal    the authenticated user
	 * @param signHashRequest  the request containing the hashes to sign
	 * @return response containing the signed hashes
	 */
	public CSCSignaturesSignHashResponse signHash(UserPrincipal userPrincipal,
												  @Valid @RequestBody CSCSignaturesSignHashRequest signHashRequest) {

		Optional<User> userOptional = userService.getUserById(userPrincipal.getId());
		if (userOptional.isEmpty()) {
			log.error("{}: User not found.", SignerError.UserNotFound.getCode());
			throw new ApiException(SignerError.UserNotFound, "User not found.");
		}

		Optional<Credential> credentialOptional = credentialService.getCredentialWithAlias(userPrincipal.getId(), signHashRequest.getCredentialID());
		if (credentialOptional.isEmpty()) {
			logCredentialError(userPrincipal, "No credential found with the given ID", signHashRequest.getCredentialID());
			throw new ApiException(CSCInvalidRequest.InvalidCredentialId, "No credential found with the given Id", signHashRequest.getCredentialID());
		}

		validateSAD(signHashRequest.getSAD(), userPrincipal);

		List<String> signedHashes = signHashRequest.getHash().stream()
				.map(hash -> signHash(hash, credentialOptional.get(), signHashRequest))
				.collect(Collectors.toList());

		logSigningEvent(userPrincipal, signHashRequest.getClientData(), signedHashes);

		CSCSignaturesSignHashResponse response = new CSCSignaturesSignHashResponse();
		response.setSignatures(signedHashes);
		return response;
	}

	private void validateSAD(String sad, UserPrincipal userPrincipal) {
		try {
			sadProvider.validateSAD(sad);
		} catch (Exception e) {
			log.error("{}: Failed to validate SAD.", SignerError.FailedToValidateSAD.getCode());
			LoggerUtil.logsUser(authProperties.getDatasourceUsername(),
					authProperties.getDatasourcePassword(), 0, userPrincipal.getId(), 6, "");
			throw new ApiException(SignerError.FailedToValidateSAD);
		}
	}

	private String signHash(String hash, Credential credential, CSCSignaturesSignHashRequest request) {
		try {
			return cryptoService.signWithPemCertificate(
					hash,
					credential.getCertificate(),
					credential.getCertificateChains(),
					credential.getPrivateKeyHSM(),
					request.getSignAlgo(),
					request.getSignAlgoParams()
			);
		} catch (Exception e) {
			log.error("{}: Failed to sign hash.", SignerError.FailedSigningData.getCode());
			LoggerUtil.logsUser(authProperties.getDatasourceUsername(),
					authProperties.getDatasourcePassword(), 0, null, 6, "");
			throw new ApiException(SignerError.FailedSigningData, "Failed to sign hash.");
		}
	}

	private void logSigningEvent(UserPrincipal userPrincipal, String pdfName, List<String> signedHashes) {
		String desc = String.format("CMS Signed Data Bytes: %s | File Name: %s", signedHashes, pdfName);
		LoggerUtil.logsUser(authProperties.getDatasourceUsername(),
				authProperties.getDatasourcePassword(), 1, userPrincipal.getId(), 6, desc);
	}

	private void logCredentialError(UserPrincipal userPrincipal, String message, String credentialAlias) {
		LoggerUtil.logsUser(authProperties.getDatasourceUsername(),
				authProperties.getDatasourcePassword(), 0, userPrincipal.getId(), 6, "");
		LoggerUtil.desc = "";
		log.error("{}: {}", CSCInvalidRequest.InvalidCredentialId.getCode(), message);
	}
}