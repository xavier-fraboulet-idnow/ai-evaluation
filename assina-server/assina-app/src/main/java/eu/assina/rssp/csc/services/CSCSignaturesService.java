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

import eu.assina.rssp.common.model.AssinaCredential;
import eu.assina.rssp.api.services.CredentialService;
import eu.assina.rssp.common.error.ApiException;
import eu.assina.rssp.crypto.AssinaCryptoService;
import eu.assina.rssp.security.UserPrincipal;
import eu.assina.csc.error.CSCInvalidRequest;
import eu.assina.csc.payload.CSCSignaturesSignHashRequest;
import eu.assina.csc.payload.CSCSignaturesSignHashResponse;
import eu.assina.csc.payload.CSCSignaturesTimestampRequest;
import eu.assina.csc.payload.CSCSignaturesTimestampResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Service
public class CSCSignaturesService {

	private static final Logger log = LoggerFactory.getLogger(CSCSignaturesService.class);
	private final CredentialService credentialService;
	private AssinaCryptoService cryptoService;
	private CSCSADProvider sadProvider;

	public CSCSignaturesService(CredentialService credentialService,
								AssinaCryptoService cryptoService,
								CSCSADProvider sadProvider) {
		this.credentialService = credentialService;
		this.cryptoService = cryptoService;
		this.sadProvider = sadProvider;
	}

	/**
	 * Sign the provided hash with the credential specified in the request
	 * @param signHashRequest
	 * @return
	 */
	public CSCSignaturesSignHashResponse signHash(UserPrincipal userPrincipal, 
			@Valid @RequestBody CSCSignaturesSignHashRequest signHashRequest) {
		CSCSignaturesSignHashResponse response = new CSCSignaturesSignHashResponse();
		final String credentialAlias = signHashRequest.getCredentialID();

		log.info("Signing hash with credential: {}", credentialAlias);
       
		final AssinaCredential credential =
				credentialService.getCredentialWithAlias(userPrincipal.getId(), credentialAlias).orElseThrow(
						() -> new ApiException(CSCInvalidRequest.InvalidCredentialId,
								"No credential found with the given Id", credentialAlias));
							
		// we know SAD is not empty thanks to annotations in the DTO, but is it valid?
        // if it is expired or otherwise invalid, the provider will throw the right
		// exception for the CSC standard
        sadProvider.validateSAD(signHashRequest.getSAD());

		// assume our sign algo has no params and our hashAlgo is implied by the signalgo
		final List<String> hashes = signHashRequest.getHash();
		List<String> signedHashes = new ArrayList<>();
		for (String hash : hashes) {
			String signedData = cryptoService.signWithPemCertificate(
					hash,
					credential.getCertificate(),
					credential.getCertificateChains(),
					credential.getPrivateKey(),
					signHashRequest.getSignAlgo(),
					signHashRequest.getSignAlgoParams());
			signedHashes.add(signedData); // assumes UTF8
		}
		response.setSignatures(signedHashes);
		return response;
	}

	public CSCSignaturesTimestampResponse generateTimestamp(CSCSignaturesTimestampRequest timestampRequest) {
		log.error("Generate Timestamp is not supported by Assina");
		throw new IllegalStateException("Not Supported by Assina Implemented"); // TODO implement this
	}
}
