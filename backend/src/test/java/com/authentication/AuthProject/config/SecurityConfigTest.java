package com.authentication.AuthProject.config;

import com.authentication.AuthProject.security.JwtAuthenticationFilter;
import com.authentication.AuthProject.security.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(jwtAuthenticationFilter, authenticationEntryPoint);
    }

    @Test
    void passwordEncoder_shouldEncodePasswords() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        String encoded = encoder.encode("Password@1");
        assertNotNull(encoded);
        assertTrue(encoder.matches("Password@1", encoded));
    }

    @Test
    void corsConfigurationSource_shouldAllowConfiguredOrigins() {
        CorsConfigurationSource source =
                securityConfig.corsConfigurationSource("http://localhost:5000, http://127.0.0.1:5000");

        assertNotNull(source);
        assertNotNull(source.getCorsConfiguration(
                new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/auth")));
    }
}
