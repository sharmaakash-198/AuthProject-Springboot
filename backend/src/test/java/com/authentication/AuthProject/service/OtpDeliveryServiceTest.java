package com.authentication.AuthProject.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class OtpDeliveryServiceTest {

    private final OtpDeliveryService otpDeliveryService = new OtpDeliveryService();

    @Test
    void sendOtp_shouldCompleteWithoutError() {
        assertDoesNotThrow(() -> otpDeliveryService.sendOtp("123456", "test@gmail.com"));
    }
}
