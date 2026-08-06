package com.authentication.AuthProject.service;

import net.spy.memcached.MemcachedClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemcachedServiceTest {

    @Mock
    private MemcachedClient memcachedClient;

    @InjectMocks
    private MemcachedService memcachedService;

    @Test
    void set_shouldDelegateToClient() {
        memcachedService.set("otp:test@gmail.com", 300, "123456");

        verify(memcachedClient).set("otp:test@gmail.com", 300, "123456");
    }

    @Test
    void get_shouldReturnStringValue() {
        when(memcachedClient.get("key")).thenReturn("value");

        assertEquals("value", memcachedService.get("key"));
    }

    @Test
    void get_shouldReturnNull_whenMissing() {
        when(memcachedClient.get("missing")).thenReturn(null);

        assertNull(memcachedService.get("missing"));
    }

    @Test
    void delete_shouldDelegateToClient() {
        memcachedService.delete("key");

        verify(memcachedClient).delete("key");
    }
}
