package eu.europa.ec.eudi.signer.rssp.security.jwt;

import io.jsonwebtoken.*;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ChatGPTJwtProviderTest {

    private JwtProviderConfig jwtConfig;
    private JwtProvider jwtProvider;

    @Before
    public void setUp() {
        jwtConfig = mock(JwtProviderConfig.class);
        when(jwtConfig.getTokenSecret()).thenReturn("testSecretKey");
        // CHATGPT: small error here due to lack to access to sub class
        when(jwtConfig.getLifetimeMinutes()).thenReturn(60L);
        when(jwtConfig.getType()).thenReturn("access");

        jwtProvider = new JwtProvider(jwtConfig);
    }

    @Test
    public void createToken_ShouldReturnValidJwtToken() {
        // Arrange
        String subject = "testUser";

        // Act
        JwtToken token = jwtProvider.createToken(subject);

        // Assert
        assertNotNull(token);
        assertNotNull(token.getRawToken());
        // CHATGPT: Fails (invert token.getSubject()) & token.getType())
        assertEquals(subject, token.getSubject());
        assertEquals("access", token.getType());
    }

    @Test
    public void parseToken_ShouldReturnJwtTokenWithCorrectClaims() {
        // Arrange
        String subject = "testUser";
        JwtToken createdToken = jwtProvider.createToken(subject);
        String rawToken = createdToken.getRawToken();

        // Act
        JwtToken parsedToken = jwtProvider.parseToken(rawToken);

        // Assert
        assertNotNull(parsedToken);
        assertEquals(subject, parsedToken.getSubject());
        assertEquals("access", parsedToken.getType());
        assertEquals(rawToken, parsedToken.getRawToken());
    }

    @Test
    public void validateToken_ShouldReturnValidJwtToken() {
        // Arrange
        String subject = "testUser";
        JwtToken createdToken = jwtProvider.createToken(subject);
        String rawToken = createdToken.getRawToken();

        // Act
        JwtToken validatedToken = jwtProvider.validateToken(rawToken);

        // Assert
        assertNotNull(validatedToken);
        assertEquals(subject, validatedToken.getSubject());
        assertEquals("access", validatedToken.getType());
    }

    @Test
    public void validateToken_ShouldReturnExpiredJwtToken() {
        // Arrange
        String subject = "testUser";
        Instant issuedAt = Instant.now().minus(2, ChronoUnit.HOURS);  // Set issue time in the past
        Instant expiration = issuedAt.plus(1, ChronoUnit.HOURS);  // Token already expired

        String expiredToken = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiration))
                .claim("type", "access")
                .signWith(SignatureAlgorithm.HS512, jwtConfig.getTokenSecret())
                .compact();

        // Act
        JwtToken validatedToken = jwtProvider.validateToken(expiredToken);

        // Assert
        assertNotNull(validatedToken);
        assertTrue(validatedToken.isExpired());
    }

    @Test
    public void validateToken_ShouldReturnInvalidJwtTokenOnSignatureException() {
        // Arrange
        String invalidToken = "invalidToken";

        // Act
        JwtToken validatedToken = jwtProvider.validateToken(invalidToken);

        // Assert
        assertNotNull(validatedToken);

        // CHATGPT: small error here due to lack to access to sub class
        assertFalse(validatedToken.isValid());
    }
}