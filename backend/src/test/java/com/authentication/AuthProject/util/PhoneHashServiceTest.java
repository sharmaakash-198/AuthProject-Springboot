package com.authentication.AuthProject.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PhoneHashServiceTest {

    private PhoneHashService phoneHashService;

    @BeforeEach
    void setUp() {
        phoneHashService = new PhoneHashService();
        String key = Base64.getEncoder().encodeToString("phone-hash-secret-key-32bytes!!".getBytes());
        ReflectionTestUtils.setField(phoneHashService, "hashKeyBase64", key);
        phoneHashService.init();
    }

    @Test
    void hash_shouldBeDeterministicAndDifferentFromInput() {
        String phone = "9876543210";

        String hash1 = phoneHashService.hash(phone);
        String hash2 = phoneHashService.hash(phone);

        assertEquals(hash1, hash2);
        assertNotEquals(phone, hash1);
        assertEquals(64, hash1.length());
    }
}
