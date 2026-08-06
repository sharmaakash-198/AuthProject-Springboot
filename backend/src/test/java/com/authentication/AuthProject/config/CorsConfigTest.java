package com.authentication.AuthProject.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CorsConfigTest {

    @Test
    void addCorsMappings_shouldRegisterApiOrigins() {
        CorsConfig config = new CorsConfig();
        ReflectionTestUtils.setField(config, "allowedOrigins",
                "http://localhost:5000, http://127.0.0.1:5000");

        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class);
        when(registry.addMapping("/api/**")).thenReturn(registration);
        when(registration.allowedOriginPatterns(any(String[].class))).thenReturn(registration);
        when(registration.allowedMethods(any(String[].class))).thenReturn(registration);
        when(registration.allowedHeaders(any(String[].class))).thenReturn(registration);
        when(registration.maxAge(anyLong())).thenReturn(registration);

        config.addCorsMappings(registry);

        verify(registry).addMapping("/api/**");
        verify(registration).allowedOriginPatterns(
                eq("http://localhost:5000"), eq("http://127.0.0.1:5000"));
        verify(registration).allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
        verify(registration).allowedHeaders("*");
        verify(registration).maxAge(3600L);
    }
}
