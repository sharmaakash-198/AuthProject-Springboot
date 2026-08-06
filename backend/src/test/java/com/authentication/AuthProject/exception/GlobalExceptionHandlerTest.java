package com.authentication.AuthProject.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleNotFound_shouldReturn404() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleNotFound(new ResourceNotFoundException("User not found."));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found.", response.getBody().getMessage());
    }

    @Test
    void handleDuplicate_shouldReturn409() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleDuplicate(new DuplicateResourceException("Email already registered."));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleCredentials_shouldReturn401() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleCredentials(new InvalidCredentialsException("Invalid email or password."));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleBadRequest_shouldReturn400() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleBadRequest(new BadRequestException("Invalid OTP."));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleUnverifiedUser_shouldReturn403() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleUnverifiedUser(new UnverifiedUserException("Email not verified."));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleRateLimitExceeded_shouldReturn429() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleRateLimitExceeded(new RateLimitExceededException("Too many attempts."));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
    }

    @Test
    void handleValidation_shouldReturnFieldErrors() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "signupRequest");
        bindingResult.addError(new FieldError("signupRequest", "email", "Email is required"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiErrorResponse> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation Failed", response.getBody().getMessage());
        assertNotNull(response.getBody().getFieldErrors());
        assertEquals("Email is required", response.getBody().getFieldErrors().get("email"));
    }

    @Test
    void handleGlobalException_shouldReturn500() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleGlobalException(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Something went wrong", response.getBody().getMessage());
    }
}
