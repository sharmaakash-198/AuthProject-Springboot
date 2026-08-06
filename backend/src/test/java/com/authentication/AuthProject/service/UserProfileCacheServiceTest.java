package com.authentication.AuthProject.service;

import com.authentication.AuthProject.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileCacheServiceTest {

    @Mock
    private MemcachedService memcachedService;

    private UserProfileCacheService cacheService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        cacheService = new UserProfileCacheService(memcachedService, objectMapper);
    }

    @Test
    void getCachedUser_shouldReturnEmpty_whenEmailBlank() {
        assertTrue(cacheService.getCachedUser("   ").isEmpty());
        verify(memcachedService, never()).get(anyString());
    }

    @Test
    void getCachedUser_shouldReturnEmpty_onCacheMiss() {
        when(memcachedService.get("user:email:test@gmail.com")).thenReturn(null);

        assertTrue(cacheService.getCachedUser("test@gmail.com").isEmpty());
    }

    @Test
    void getCachedUser_shouldReturnUser_onCacheHit() throws Exception {
        UserResponse cached = UserResponse.builder()
                .id(1L)
                .email("test@gmail.com")
                .firstName("Akash")
                .build();
        when(memcachedService.get("user:email:test@gmail.com"))
                .thenReturn(objectMapper.writeValueAsString(cached));

        Optional<UserResponse> result = cacheService.getCachedUser("  Test@Gmail.com ");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Akash", result.get().getFirstName());
    }

    @Test
    void getCachedUser_shouldDeleteCorruptEntry_andReturnEmpty() {
        when(memcachedService.get("user:email:test@gmail.com")).thenReturn("{bad-json");

        assertTrue(cacheService.getCachedUser("test@gmail.com").isEmpty());
        verify(memcachedService).delete("user:email:test@gmail.com");
    }

    @Test
    void cacheUser_shouldSkip_whenResponseInvalid() {
        cacheService.cacheUser(null);
        cacheService.cacheUser(UserResponse.builder().email("  ").build());

        verify(memcachedService, never()).set(anyString(), anyInt(), anyString());
    }

    @Test
    void cacheUser_shouldStoreSerializedProfile() {
        UserResponse response = UserResponse.builder()
                .id(1L)
                .email("test@gmail.com")
                .firstName("Akash")
                .build();

        cacheService.cacheUser(response);

        verify(memcachedService).set(eq("user:email:test@gmail.com"), eq(600), anyString());
    }

    @Test
    void cacheUser_shouldSwallowMemcachedErrors() {
        UserResponse response = UserResponse.builder()
                .id(1L)
                .email("test@gmail.com")
                .build();
        doThrow(new RuntimeException("down"))
                .when(memcachedService).set(anyString(), anyInt(), anyString());

        cacheService.cacheUser(response);
    }

    @Test
    void evictUser_shouldSkip_whenEmailBlank() {
        cacheService.evictUser(null);
        cacheService.evictUser(" ");

        verify(memcachedService, never()).delete(anyString());
    }

    @Test
    void evictUser_shouldDeleteCacheKey() {
        cacheService.evictUser("test@gmail.com");

        verify(memcachedService).delete("user:email:test@gmail.com");
    }

    @Test
    void evictUser_shouldSwallowErrors() {
        doThrow(new RuntimeException("down"))
                .when(memcachedService).delete("user:email:test@gmail.com");

        cacheService.evictUser("test@gmail.com");
    }
}
