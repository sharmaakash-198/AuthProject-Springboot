package com.authentication.AuthProject.service;

import com.authentication.AuthProject.dto.request.LoginRequest;
import com.authentication.AuthProject.dto.request.ResendOtpRequest;
import com.authentication.AuthProject.dto.request.SignupRequest;
import com.authentication.AuthProject.dto.request.VerifyOtpRequest;
import com.authentication.AuthProject.dto.response.AuthResponse;
import com.authentication.AuthProject.entity.User;
import com.authentication.AuthProject.enums.Gender;
import com.authentication.AuthProject.exception.DuplicateResourceException;
import com.authentication.AuthProject.exception.InvalidCredentialsException;
import com.authentication.AuthProject.exception.UnverifiedUserException;
import com.authentication.AuthProject.repository.UserRepository;
import com.authentication.AuthProject.security.JwtService;
import com.authentication.AuthProject.util.EncryptionService;
import com.authentication.AuthProject.util.PhoneHashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private PhoneHashService phoneHashService;

    @Mock
    private OtpService otpService;

    @Mock
    private UserService userService;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private JwtService jwtService;

    private SignupRequest signupRequest;

    @BeforeEach
    void setUp() {
        signupRequest = SignupRequest.builder()
                .firstName("Akash")
                .lastName("Sharma")
                .dob(LocalDate.of(2000, 1, 1))
                .gender(Gender.MALE)
                .email("  Test@Gmail.com ")
                .phoneNumber("9876543210")
                .password("Password@1")
                .build();
    }

    @Test
    void signup_shouldThrowException_whenEmailAlreadyExists() {
        when(repository.existsByEmail("test@gmail.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.signup(signupRequest));

        verify(repository).existsByEmail("test@gmail.com");
        verify(repository, never()).save(any());
    }

    @Test
    void signup_shouldThrowException_whenPhoneAlreadyExists() {
        when(repository.existsByEmail("test@gmail.com")).thenReturn(false);
        when(phoneHashService.hash("9876543210")).thenReturn("phone-hash");
        when(repository.existsByPhoneNumberHash("phone-hash")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.signup(signupRequest));

        verify(repository, never()).save(any());
    }

    @Test
    void signup_shouldSaveUserAndIssueOtp_whenRequestIsValid() {
        when(repository.existsByEmail("test@gmail.com")).thenReturn(false);
        when(phoneHashService.hash("9876543210")).thenReturn("phone-hash");
        when(repository.existsByPhoneNumberHash("phone-hash")).thenReturn(false);
        when(encryptionService.encrypt("9876543210")).thenReturn("encrypted-phone");
        when(passwordEncoder.encode("Password@1")).thenReturn("encoded-password");

        User savedUser = User.builder()
                .id(10L)
                .email("test@gmail.com")
                .build();
        when(repository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse response = authService.signup(signupRequest);

        assertEquals(10L, response.getUserId());
        assertEquals("Signup successful. Verify OTP sent to your email.", response.getMessage());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(userCaptor.capture());
        User captured = userCaptor.getValue();
        assertEquals("test@gmail.com", captured.getEmail());
        assertEquals("encrypted-phone", captured.getPhoneNumber());
        assertEquals("phone-hash", captured.getPhoneNumberHash());
        assertEquals("encoded-password", captured.getPassword());
        assertFalse(captured.isVerified());

        verify(otpService).issueOtpForSignup("test@gmail.com");
    }

    @Test
    void login_shouldThrowInvalidCredentials_whenUserNotFound() {
        LoginRequest request = LoginRequest.builder()
                .email("missing@gmail.com")
                .password("Password@1")
                .build();

        when(repository.findByEmail("missing@gmail.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        verify(rateLimitService).recordFailedAttempt("missing@gmail.com");
    }

    @Test
    void login_shouldThrowInvalidCredentials_whenPasswordDoesNotMatch() {
        LoginRequest request = LoginRequest.builder()
                .email("test@gmail.com")
                .password("WrongPass@1")
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("encoded")
                .verified(true)
                .build();

        when(repository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        doNothing().when(rateLimitService).checkLoginBlocked("test@gmail.com");
        when(passwordEncoder.matches("WrongPass@1", "encoded")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        verify(rateLimitService).recordFailedAttempt("test@gmail.com");
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_shouldThrowUnverifiedUser_whenEmailNotVerified() {
        LoginRequest request = LoginRequest.builder()
                .email("test@gmail.com")
                .password("Password@1")
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("encoded")
                .verified(false)
                .build();

        when(repository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        doNothing().when(rateLimitService).checkLoginBlocked("test@gmail.com");
        when(passwordEncoder.matches("Password@1", "encoded")).thenReturn(true);

        assertThrows(UnverifiedUserException.class, () -> authService.login(request));
        verify(rateLimitService).resetLoginLimit("test@gmail.com");
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        LoginRequest request = LoginRequest.builder()
                .email("test@gmail.com")
                .password("Password@1")
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("encoded")
                .verified(true)
                .build();

        when(repository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        doNothing().when(rateLimitService).checkLoginBlocked("test@gmail.com");
        when(passwordEncoder.matches("Password@1", "encoded")).thenReturn(true);
        when(jwtService.generateToken("test@gmail.com")).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals(1L, response.getUserId());
        assertEquals("Login successful", response.getMessage());
        assertEquals("jwt-token", response.getToken());
        verify(rateLimitService).resetLoginLimit("test@gmail.com");
        verify(userService).warmProfileCache(user);
    }

    @Test
    void verifyOtp_shouldReturnSuccessMessage() {
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .email("test@gmail.com")
                .otp("123456")
                .build();

        AuthResponse response = authService.verifyOtp(request);

        assertEquals("Email verified successfully.", response.getMessage());
        verify(otpService).verifyOtp("test@gmail.com", "123456");
    }

    @Test
    void resendOtp_shouldReturnSuccessMessage() {
        ResendOtpRequest request = ResendOtpRequest.builder()
                .email("test@gmail.com")
                .build();

        AuthResponse response = authService.resendOtp(request);

        assertEquals("OTP sent to your email.", response.getMessage());
        verify(otpService).resendOtp("test@gmail.com");
    }
}
