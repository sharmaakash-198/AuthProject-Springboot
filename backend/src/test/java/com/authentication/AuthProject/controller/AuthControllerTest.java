package com.authentication.AuthProject.controller;

import com.authentication.AuthProject.dto.request.LoginRequest;
import com.authentication.AuthProject.dto.request.ResendOtpRequest;
import com.authentication.AuthProject.dto.request.SignupRequest;
import com.authentication.AuthProject.dto.request.VerifyOtpRequest;
import com.authentication.AuthProject.dto.response.AuthResponse;
import com.authentication.AuthProject.exception.GlobalExceptionHandler;
import com.authentication.AuthProject.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .build();
    }

    @Test
    void greet_shouldReturnRunningMessage() throws Exception {
        mockMvc.perform(get("/api/auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Authentication API is Running"));
    }

    @Test
    void signup_shouldReturn201() throws Exception {
        when(authService.signup(any(SignupRequest.class)))
                .thenReturn(AuthResponse.builder()
                        .userId(1L)
                        .message("Signup successful. Verify OTP sent to your email.")
                        .build());

        String body = """
                {
                  "firstName": "Akash",
                  "lastName": "Sharma",
                  "dob": "2000-01-01",
                  "gender": "MALE",
                  "email": "test@gmail.com",
                  "phoneNumber": "9876543210",
                  "password": "Password@1"
                }
                """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.message").value("Signup successful. Verify OTP sent to your email."));
    }

    @Test
    void signup_shouldReturn400_whenValidationFails() throws Exception {
        String body = """
                {
                  "firstName": "",
                  "email": "not-an-email",
                  "phoneNumber": "123",
                  "password": "weak"
                }
                """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"));
    }

    @Test
    void login_shouldReturn200() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(AuthResponse.builder()
                        .userId(1L)
                        .message("Login successful")
                        .token("jwt-token")
                        .build());

        String body = """
                {
                  "email": "test@gmail.com",
                  "password": "Password@1"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void verifyOtp_shouldReturn200() throws Exception {
        when(authService.verifyOtp(any(VerifyOtpRequest.class)))
                .thenReturn(AuthResponse.builder()
                        .message("Email verified successfully.")
                        .build());

        String body = """
                {
                  "email": "test@gmail.com",
                  "otp": "123456"
                }
                """;

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified successfully."));
    }

    @Test
    void resendOtp_shouldReturn200() throws Exception {
        when(authService.resendOtp(any(ResendOtpRequest.class)))
                .thenReturn(AuthResponse.builder()
                        .message("OTP sent to your email.")
                        .build());

        String body = """
                {
                  "email": "test@gmail.com"
                }
                """;

        mockMvc.perform(post("/api/auth/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP sent to your email."));
    }
}
