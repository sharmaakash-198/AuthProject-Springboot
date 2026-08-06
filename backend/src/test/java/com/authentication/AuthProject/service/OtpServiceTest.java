package com.authentication.AuthProject.service;

import com.authentication.AuthProject.entity.User;
import com.authentication.AuthProject.exception.BadRequestException;
import com.authentication.AuthProject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private MemcachedService memcachedService;

    @Mock
    private OtpDeliveryService otpDeliveryService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OtpService otpService;

    @Test
    void issueOtpForSignup_shouldStoreOtpAndSend() {
        User user = User.builder().email("test@gmail.com").verified(false).build();
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));

        otpService.issueOtpForSignup("  Test@Gmail.com ");

        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(memcachedService).set(eq("otp:test@gmail.com"), eq(300), otpCaptor.capture());
        verify(memcachedService).set(eq("otp:attempts:test@gmail.com"), eq(300), eq("0"));
        verify(otpDeliveryService).sendOtp(otpCaptor.getValue(), "test@gmail.com");
        assertEquals(6, otpCaptor.getValue().length());
    }

    @Test
    void issueOtpForSignup_shouldThrow_whenAlreadyVerified() {
        User user = User.builder().email("test@gmail.com").verified(true).build();
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> otpService.issueOtpForSignup("test@gmail.com"));
        verify(memcachedService, never()).set(anyString(), anyInt(), anyString());
    }

    @Test
    void verifyOtp_shouldMarkUserVerified_whenOtpMatches() {
        User user = User.builder().email("test@gmail.com").verified(false).build();
        when(memcachedService.get("otp:attempts:test@gmail.com")).thenReturn("0");
        when(memcachedService.get("otp:test@gmail.com")).thenReturn("123456");
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));

        otpService.verifyOtp("test@gmail.com", "123456");

        assertTrue(user.isVerified());
        verify(userRepository).save(user);
        verify(memcachedService).delete("otp:test@gmail.com");
        verify(memcachedService).delete("otp:attempts:test@gmail.com");
    }

    @Test
    void verifyOtp_shouldThrow_whenOtpInvalid() {
        when(memcachedService.get("otp:attempts:test@gmail.com")).thenReturn("0");
        when(memcachedService.get("otp:test@gmail.com")).thenReturn("123456");

        assertThrows(BadRequestException.class, () -> otpService.verifyOtp("test@gmail.com", "000000"));
        verify(memcachedService).set(eq("otp:attempts:test@gmail.com"), eq(300), eq("1"));
    }

    @Test
    void verifyOtp_shouldThrow_whenMaxAttemptsExceeded() {
        when(memcachedService.get("otp:attempts:test@gmail.com")).thenReturn("3");

        assertThrows(BadRequestException.class, () -> otpService.verifyOtp("test@gmail.com", "123456"));
        verify(memcachedService, never()).get("otp:test@gmail.com");
    }

    @Test
    void resendOtp_shouldThrow_whenUserNotFound() {
        when(userRepository.findByEmail("missing@gmail.com")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> otpService.resendOtp("missing@gmail.com"));
    }

    @Test
    void resendOtp_shouldClearOldKeysAndIssueNewOtp() {
        User user = User.builder().email("test@gmail.com").verified(false).build();
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));

        otpService.resendOtp("test@gmail.com");

        verify(memcachedService).delete("otp:test@gmail.com");
        verify(memcachedService).delete("otp:attempts:test@gmail.com");
        verify(otpDeliveryService).sendOtp(anyString(), eq("test@gmail.com"));
    }
}
