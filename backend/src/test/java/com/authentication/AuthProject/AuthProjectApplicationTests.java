package com.authentication.AuthProject;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Full-context boot requires Postgres, Redis, Memcached, and env secrets.
 * Covered by focused unit/slice tests instead.
 */
@Disabled("Requires external infrastructure and secrets; use unit/slice tests.")
@SpringBootTest
class AuthProjectApplicationTests {

    @Test
    void contextLoads() {
    }
}
