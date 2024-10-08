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
import java.util.function.Supplier;

@Service
public class AmazonQBuiltinRefactoCSCSignaturesService {

    private static final Logger log = LoggerFactory.getLogger(CSCSignaturesService.class);

    private final CredentialService credentialService;
    private final UserService userService;
    private final CryptoService cryptoService;
    private final CSCSADProvider sadProvider;
    private final AuthProperties authProperties;

    public AmazonQBuiltinRefactoCSCSignaturesService(CredentialService credentialService, UserService userService,
                                CryptoService cryptoService, CSCSADProvider sadProvider, AuthProperties authProperties) {
        this.credentialService = credentialService;
        this.userService = userService;
        this.cryptoService = cryptoService;
        this.sadProvider = sadProvider;
        this.authProperties = authProperties;
    }

    public CSCSignaturesSignHashResponse signHash(UserPrincipal userPrincipal,
                                                  @Valid @RequestBody CSCSignaturesSignHashRequest signHashRequest) {
        return executeWithErrorHandling(() -> {
            User user = getUser(userPrincipal);
            Credential credential = getCredential(userPrincipal, signHashRequest.getCredentialID());
            validateSAD(signHashRequest.getSAD());

            List<String> signedHashes = signHashes(credential, signHashRequest);

            logSuccessfulSigning(userPrincipal, signHashRequest.getClientData(), signedHashes);

            CSCSignaturesSignHashResponse response = new CSCSignaturesSignHashResponse();
            response.setSignatures(signedHashes);
            return response;
        });
    }

    private <T> T executeWithErrorHandling(Supplier<T> action) {
        try {
            return action.get();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred", e);
            throw new ApiException(SignerError.UnexpectedError, e.getMessage());
        }
    }

    private User getUser(UserPrincipal userPrincipal) {
        return userService.getUserById(userPrincipal.getId())
                .orElseThrow(() -> new ApiException(SignerError.UserNotFound, "User not found."));
    }

    private Credential getCredential(UserPrincipal userPrincipal, String credentialAlias) {
        return credentialService.getCredentialWithAlias(userPrincipal.getId(), credentialAlias)
                .orElseThrow(() -> {
                    logFailure(userPrincipal);
                    return new ApiException(CSCInvalidRequest.InvalidCredentialId,
                            "No credential found with the given Id", credentialAlias);
                });
    }

    private void validateSAD(String sad) {
        try {
            sadProvider.validateSAD(sad);
        } catch (Exception e) {
            throw new ApiException(SignerError.FailedToValidateSAD, e.getMessage());
        }
    }

    private List<String> signHashes(Credential credential, CSCSignaturesSignHashRequest signHashRequest) {
        return signHashRequest.getHash().stream()
                .map(hash -> cryptoService.signWithPemCertificate(
                        hash,
                        credential.getCertificate(),
                        credential.getCertificateChains(),
                        credential.getPrivateKeyHSM(),
                        signHashRequest.getSignAlgo(),
                        signHashRequest.getSignAlgoParams()))
                .toList();
    }

    private void logSuccessfulSigning(UserPrincipal userPrincipal, String pdfName, List<String> signedHashes) {
        String logMessage = String.format("CMS Signed Data Bytes: %s | File Name: %s", signedHashes, pdfName);
        LoggerUtil.logsUser(authProperties.getDatasourceUsername(),
                authProperties.getDatasourcePassword(), 1, userPrincipal.getId(), 6, logMessage);
    }

    private void logFailure(UserPrincipal userPrincipal) {
        LoggerUtil.logsUser(authProperties.getDatasourceUsername(),
                authProperties.getDatasourcePassword(), 0, userPrincipal.getId(), 6, "");
    }
}