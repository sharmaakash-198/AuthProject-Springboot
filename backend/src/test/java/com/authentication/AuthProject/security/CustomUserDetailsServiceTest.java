package com.authentication.AuthProject.security;

import com.authentication.AuthProject.entity.User;
import com.authentication.AuthProject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExistsAndVerified() {
        User user = User.builder()
                .email("test@gmail.com")
                .password("encoded")
                .verified(true)
                .build();

        when(repository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("  Test@Gmail.com ");

        assertEquals("test@gmail.com", details.getUsername());
        assertEquals("encoded", details.getPassword());
        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_shouldDisableUser_whenNotVerified() {
        User user = User.builder()
                .email("test@gmail.com")
                .password("encoded")
                .verified(false)
                .build();

        when(repository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("test@gmail.com");

        assertFalse(details.isEnabled());
    }

    @Test
    void loadUserByUsername_shouldThrow_whenUserMissing() {
        when(repository.findByEmail("missing@gmail.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("missing@gmail.com"));
    }
}
