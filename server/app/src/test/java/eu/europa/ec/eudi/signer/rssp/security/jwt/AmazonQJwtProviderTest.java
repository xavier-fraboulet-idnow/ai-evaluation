package eu.europa.ec.eudi.signer.rssp.security.jwt;

import io.jsonwebtoken.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AmazonQJwtProviderTest {


    @Mock
    private JwtProviderConfig jwtConfig;

    private JwtProvider jwtProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(jwtConfig.getType()).thenReturn("testType");
        when(jwtConfig.getTokenSecret()).thenReturn("testSecret");
        when(jwtConfig.getLifetimeMinutes()).thenReturn(60L);
        jwtProvider = new JwtProvider(jwtConfig);
    }

    @Test
    public void testCreateToken() {
        String subject = "testSubject";
        JwtToken token = jwtProvider.createToken(subject);

        assertNotNull(token);
        assertEquals(subject, token.getSubject());
        assertEquals("testType", token.getType());
        assertNotNull(token.getRawToken());
    }

    @Test
    public void testParseToken() {
        String subject = "testSubject";
        JwtToken createdToken = jwtProvider.createToken(subject);
        JwtToken parsedToken = jwtProvider.parseToken(createdToken.getRawToken());

        assertNotNull(parsedToken);
        assertEquals(subject, parsedToken.getSubject());
        assertEquals("testType", parsedToken.getType());
        assertEquals(createdToken.getRawToken(), parsedToken.getRawToken());
    }

    @Test
    public void testValidateValidToken() {
        String subject = "testSubject";
        JwtToken createdToken = jwtProvider.createToken(subject);
        JwtToken validatedToken = jwtProvider.validateToken(createdToken.getRawToken());

        assertNotNull(validatedToken);
        assertEquals(subject, validatedToken.getSubject());
        assertEquals("testType", validatedToken.getType());
        assertFalse(!validatedToken.isValid());
        assertFalse(validatedToken.isExpired());
    }

    @Test
    public void testValidateExpiredToken() {
        String subject = "testSubject";
        String expiredToken = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis() - 3600000))
                .setExpiration(new Date(System.currentTimeMillis() - 1800000))
                .claim("type", "testType")
                .signWith(SignatureAlgorithm.HS512, "testSecret")
                .compact();

        JwtToken validatedToken = jwtProvider.validateToken(expiredToken);

        assertNotNull(validatedToken);
        assertTrue(validatedToken.isExpired());
    }

    @Test
    public void testValidateInvalidToken() {
        String invalidToken = "invalidToken";
        JwtToken validatedToken = jwtProvider.validateToken(invalidToken);

        assertNotNull(validatedToken);
        assertTrue(!validatedToken.isValid());
    }

    @Test
    public void testValidateTokenWithWrongType() {
        String subject = "testSubject";
        String wrongTypeToken = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .claim("type", "wrongType")
                .signWith(SignatureAlgorithm.HS512, "testSecret")
                .compact();

        JwtToken validatedToken = jwtProvider.validateToken(wrongTypeToken);

        assertNotNull(validatedToken);
        assertTrue(!validatedToken.isValid());
        assertEquals("Unexpected token type: should be of type testType", validatedToken.getError());
    }
}