/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.security;

import static org.junit.jupiter.api.Assertions.*;

import com.jlpt.feature.student.StudentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(
                jwtProvider, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtProvider, "jwtAccessExpirationMs", 900000L);
        ReflectionTestUtils.setField(jwtProvider, "jwtRefreshExpirationMs", 604800000L);

        StudentUser user = new StudentUser();
        user.setEmail("test@example.com");
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    void testGenerateAndValidateAccessToken() {
        String token = jwtProvider.generateAccessToken(auth);
        assertNotNull(token);
        assertTrue(jwtProvider.validateJwtToken(token));
        assertEquals("test@example.com", jwtProvider.getUserNameFromJwtToken(token));
    }

    @Test
    void testGenerateAndValidateRefreshToken() {
        String token = jwtProvider.generateRefreshToken(auth);
        assertNotNull(token);
        assertTrue(jwtProvider.validateJwtToken(token));
    }

    @Test
    void testValidateInvalidToken() {
        assertFalse(jwtProvider.validateJwtToken("invalid.token.here"));
    }

    @Test
    void testGenerateLimitedSessionTokenContainsStaffClaims() {
        String token = jwtProvider.generateLimitedSessionToken(7L, "staff@example.com");

        assertNotNull(token);
        assertTrue(jwtProvider.validateJwtToken(token));
        assertEquals("staff@example.com", jwtProvider.getUserNameFromJwtToken(token));
        assertEquals("STAFF", jwtProvider.getRoleFromToken(token));
        assertEquals("limited_session", jwtProvider.getTokenTypeFromToken(token));
        assertEquals(7L, jwtProvider.getStaffIdFromToken(token));
    }
}
