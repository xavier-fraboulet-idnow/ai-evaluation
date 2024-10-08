package eu.europa.ec.eudi.signer.rssp.csc.services;

import eu.europa.ec.eudi.signer.csc.payload.CSCSignaturesSignHashRequest;
import eu.europa.ec.eudi.signer.csc.payload.CSCSignaturesSignHashResponse;
import eu.europa.ec.eudi.signer.rssp.api.services.CredentialService;
import eu.europa.ec.eudi.signer.rssp.api.services.UserService;
import eu.europa.ec.eudi.signer.rssp.common.config.AuthProperties;
import eu.europa.ec.eudi.signer.rssp.common.error.ApiException;
import eu.europa.ec.eudi.signer.rssp.common.error.SignerError;
import eu.europa.ec.eudi.signer.rssp.crypto.CryptoService;
import eu.europa.ec.eudi.signer.rssp.csc.services.CSCSADProvider;
import eu.europa.ec.eudi.signer.rssp.csc.services.CSCSignaturesService;

import eu.europa.ec.eudi.signer.rssp.entities.Credential;
import eu.europa.ec.eudi.signer.rssp.entities.User;
import eu.europa.ec.eudi.signer.rssp.security.UserPrincipal;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

public class CSCSignaturesServiceTest {

    private UserService userService = mock(UserService.class);
    private CredentialService credentialService = mock(CredentialService.class);
    private CryptoService cryptoService = mock(CryptoService.class);
    private CSCSADProvider sadProvider = mock(CSCSADProvider.class);
    private AuthProperties authProperties = mock(AuthProperties.class);

    private CopilotRefactoCSCSignaturesService cscSignaturesService = new CopilotRefactoCSCSignaturesService(credentialService, userService, cryptoService, sadProvider, authProperties);

    // Successfully signs a hash when valid user and credential are provided
    @Test
    public void test_sign_hash_success() {
        // Arrange
        UserPrincipal userPrincipal = new UserPrincipal("userId", "userHash", "John", "Doe", List.of());
        CSCSignaturesSignHashRequest request = new CSCSignaturesSignHashRequest();
        request.setCredentialID("validCredentialId");
        request.setSAD("validSAD");
        request.setHash(List.of("hash1", "hash2"));
        request.setSignAlgo("signAlgo");
        request.setSignAlgoParams("signAlgoParams");

        User user = new User("userId");
        Credential credential = new Credential();
        credential.setCertificate("certificate");
        credential.setCertificateChains(List.of());
        credential.setPrivateKeyHSM(new byte[]{});

        when(userService.getUserById("userId")).thenReturn(Optional.of(user));
        when(credentialService.getCredentialWithAlias("userId", "validCredentialId")).thenReturn(Optional.of(credential));
        doNothing().when(sadProvider).validateSAD("validSAD");
        when(cryptoService.signWithPemCertificate(anyString(), anyString(), anyList(), any(), anyString(), anyString()))
                .thenReturn("signedHash");

        // Act
        CSCSignaturesSignHashResponse response = cscSignaturesService.signHash(userPrincipal, request);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getSignatures().size());
    }

    // Throws an exception when the user is not found
    @Test
    public void test_sign_hash_user_not_found() {
        // Arrange
        UserPrincipal userPrincipal = new UserPrincipal("invalidUserId", "userHash", "John", "Doe", List.of());
        CSCSignaturesSignHashRequest request = new CSCSignaturesSignHashRequest();
        request.setCredentialID("validCredentialId");
        request.setSAD("validSAD");

        when(userService.getUserById("invalidUserId")).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            cscSignaturesService.signHash(userPrincipal, request);
        });

        assertEquals(SignerError.UserNotFound, exception.getApiError());
    }

    // Validates and processes multiple hashes in a single request
    @Test
    public void test_sign_multiple_hashes() {
        // Arrange
        UserPrincipal userPrincipal = new UserPrincipal("userId", "userHash", "John", "Doe", List.of());
        CSCSignaturesSignHashRequest request = new CSCSignaturesSignHashRequest();
        request.setCredentialID("validCredentialId");
        request.setSAD("validSAD");
        request.setHash(List.of("hash1", "hash2"));
        request.setSignAlgo("signAlgo");
        request.setSignAlgoParams("signAlgoParams");

        User user = new User("userId");
        Credential credential = new Credential();
        credential.setCertificate("certificate");
        credential.setCertificateChains(List.of());
        credential.setPrivateKeyHSM(new byte[]{});

        when(userService.getUserById("userId")).thenReturn(Optional.of(user));
        when(credentialService.getCredentialWithAlias("userId", "validCredentialId")).thenReturn(Optional.of(credential));
        doNothing().when(sadProvider).validateSAD("validSAD");
        when(cryptoService.signWithPemCertificate(anyString(), anyString(), anyList(), any(), anyString(), anyString()))
                .thenReturn("signedHash");

        // Act
        CSCSignaturesSignHashResponse response = cscSignaturesService.signHash(userPrincipal, request);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getSignatures().size());
    }

    // Logs successful signing operations with appropriate details
    @Test
    public void test_sign_hash_logging() {
        // Arrange
        UserPrincipal userPrincipal = new UserPrincipal("userId", "userHash", "John", "Doe", List.of());
        CSCSignaturesSignHashRequest request = new CSCSignaturesSignHashRequest();
        request.setCredentialID("validCredentialId");
        request.setSAD("validSAD");
        request.setHash(List.of("hash1", "hash2"));
        request.setSignAlgo("signAlgo");
        request.setSignAlgoParams("signAlgoParams");

        User user = new User("userId");
        Credential credential = new Credential();
        credential.setCertificate("certificate");
        credential.setCertificateChains(List.of());
        credential.setPrivateKeyHSM(new byte[]{});

        when(userService.getUserById("userId")).thenReturn(Optional.of(user));
        when(credentialService.getCredentialWithAlias("userId", "validCredentialId")).thenReturn(Optional.of(credential));
        doNothing().when(sadProvider).validateSAD("validSAD");
        when(cryptoService.signWithPemCertificate(anyString(), anyString(), anyList(), any(), anyString(), anyString()))
                .thenReturn("signedHash");

        // Act
        CSCSignaturesSignHashResponse response = cscSignaturesService.signHash(userPrincipal, request);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getSignatures().size());
    }

    // Throws an exception when the credential is not found
    @Test
    public void test_sign_hash_credential_not_found() {
        // Arrange
        UserPrincipal userPrincipal = new UserPrincipal("userId", "userHash", "John", "Doe", List.of());
        CSCSignaturesSignHashRequest request = new CSCSignaturesSignHashRequest();
        request.setCredentialID("nonExistentCredentialId");
        request.setSAD("validSAD");
        request.setHash(List.of("hash1", "hash2"));
        request.setSignAlgo("signAlgo");
        request.setSignAlgoParams("signAlgoParams");

        User user = new User("userId");

        when(userService.getUserById("userId")).thenReturn(Optional.of(user));
        when(credentialService.getCredentialWithAlias("userId", "nonExistentCredentialId")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApiException.class, () -> cscSignaturesService.signHash(userPrincipal, request));
    }

    // Handles invalid or expired SAD by throwing an appropriate exception
    @Test
    public void test_handles_invalid_or_expired_sad_by_throwing_exception() {
        // Arrange
        UserPrincipal userPrincipal = new UserPrincipal("userId", "userHash", "John", "Doe", List.of());
        CSCSignaturesSignHashRequest request = new CSCSignaturesSignHashRequest();
        request.setCredentialID("validCredentialId");
        request.setSAD("invalidSAD");
        request.setHash(List.of("hash1", "hash2"));
        request.setSignAlgo("signAlgo");
        request.setSignAlgoParams("signAlgoParams");

        User user = new User("userId");
        Credential credential = new Credential();
        credential.setCertificate("certificate");
        credential.setCertificateChains(List.of());
        credential.setPrivateKeyHSM(new byte[]{});

        when(userService.getUserById("userId")).thenReturn(Optional.of(user));
        when(credentialService.getCredentialWithAlias("userId", "validCredentialId")).thenReturn(Optional.of(credential));
        doThrow(new ApiException(SignerError.FailedToValidateSAD)).when(sadProvider).validateSAD("invalidSAD");

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            cscSignaturesService.signHash(userPrincipal, request);
        });

        assertEquals(SignerError.FailedToValidateSAD, exception.getApiError());
    }
}