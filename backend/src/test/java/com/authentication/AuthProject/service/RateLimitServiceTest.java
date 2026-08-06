package com.authentication.AuthProject.service;

import com.authentication.AuthProject.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RateLimitService rateLimitService;

    @Test
    void checkLoginBlocked_shouldPass_whenNoAttempts() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("login:test@gmail.com")).thenReturn(null);

        assertDoesNotThrow(() -> rateLimitService.checkLoginBlocked("test@gmail.com"));
    }

    @Test
    void checkLoginBlocked_shouldThrow_whenAttemptsExceeded() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("login:test@gmail.com")).thenReturn("5");

        assertThrows(RateLimitExceededException.class,
                () -> rateLimitService.checkLoginBlocked("  Test@Gmail.com "));
    }

    @Test
    void recordFailedAttempt_shouldSetExpire_onFirstAttempt() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("login:test@gmail.com")).thenReturn(1L);

        rateLimitService.recordFailedAttempt("test@gmail.com");

        verify(redisTemplate).expire("login:test@gmail.com", Duration.ofMinutes(1));
    }

    @Test
    void resetLoginLimit_shouldDeleteKey() {
        rateLimitService.resetLoginLimit("test@gmail.com");

        verify(redisTemplate).delete("login:test@gmail.com");
    }
}
