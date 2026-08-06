package com.authentication.AuthProject.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "01234567890123456789012345678901"); // 32 chars for HS256
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3_600_000L);
    }

    @Test
    void generateToken_and_extractUsername_shouldNormalizeEmail() {
        String token = jwtService.generateToken("  Test@Gmail.com ");

        assertEquals("test@gmail.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, "TEST@gmail.com"));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenEmailDoesNotMatch() {
        String token = jwtService.generateToken("user@gmail.com");

        assertFalse(jwtService.isTokenValid(token, "other@gmail.com"));
    }

    @Test
    void isTokenValid_shouldRejectExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1_000L);
        String token = jwtService.generateToken("user@gmail.com");

        assertThrows(ExpiredJwtException.class,
                () -> jwtService.isTokenValid(token, "user@gmail.com"));
    }
}
