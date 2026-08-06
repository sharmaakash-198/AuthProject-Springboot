package com.authentication.AuthProject.security;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    void commence_shouldWriteUnauthorizedJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint(objectMapper);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ServletOutputStream outputStream = new ServletOutputStream() {
            @Override
            public void write(int b) {
                buffer.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }
        };
        when(response.getOutputStream()).thenReturn(outputStream);

        entryPoint.commence(request, response, new BadCredentialsException("bad"));

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);

        JsonNode body = objectMapper.readTree(buffer.toString(StandardCharsets.UTF_8));
        assertEquals(401, body.get("status").asInt());
        assertEquals("Unauthorized", body.get("error").asString());
        assertEquals("Authentication required.", body.get("message").asString());
    }
}
