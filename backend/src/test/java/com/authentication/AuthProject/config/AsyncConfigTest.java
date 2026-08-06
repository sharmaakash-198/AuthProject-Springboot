package com.authentication.AuthProject.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncConfigTest {

    @Test
    void otpTaskExecutor_shouldConfigureThreadPool() {
        AsyncConfig config = new AsyncConfig();

        Executor executor = config.otpTaskExecutor();

        assertInstanceOf(ThreadPoolTaskExecutor.class, executor);
        ThreadPoolTaskExecutor pool = (ThreadPoolTaskExecutor) executor;
        assertEquals(2, pool.getCorePoolSize());
        assertEquals(5, pool.getMaxPoolSize());
        assertTrue(pool.getThreadNamePrefix().startsWith("otp-async-"));
        pool.shutdown();
    }
}
