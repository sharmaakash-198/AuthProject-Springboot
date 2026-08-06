package com.authentication.AuthProject.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService();
        String key = Base64.getEncoder().encodeToString(new byte[32]);
        ReflectionTestUtils.setField(encryptionService, "secretKeyBase64", key);
        encryptionService.init();
    }

    @Test
    void encryptAndDecrypt_shouldRoundTrip() {
        String plainText = "9876543210";

        String encrypted = encryptionService.encrypt(plainText);
        String decrypted = encryptionService.decrypt(encrypted);

        assertNotEquals(plainText, encrypted);
        assertEquals(plainText, decrypted);
    }
}
