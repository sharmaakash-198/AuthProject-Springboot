package com.authentication.AuthProject.service;

import com.authentication.AuthProject.dto.request.ChangePasswordRequest;
import com.authentication.AuthProject.dto.request.UpdateProfileRequest;
import com.authentication.AuthProject.dto.response.UserResponse;
import com.authentication.AuthProject.entity.User;
import com.authentication.AuthProject.enums.Gender;
import com.authentication.AuthProject.exception.BadRequestException;
import com.authentication.AuthProject.exception.DuplicateResourceException;
import com.authentication.AuthProject.exception.InvalidCredentialsException;
import com.authentication.AuthProject.exception.ResourceNotFoundException;
import com.authentication.AuthProject.repository.UserRepository;
import com.authentication.AuthProject.util.EncryptionService;
import com.authentication.AuthProject.util.PhoneHashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private PhoneHashService phoneHashService;

    @Mock
    private UserProfileCacheService userProfileCacheService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("Akash")
                .lastName("Sharma")
                .dob(LocalDate.of(2000, 1, 1))
                .gender(Gender.MALE)
                .email("test@gmail.com")
                .phoneNumber("encrypted-phone")
                .phoneNumberHash("phone-hash")
                .password("encoded-password")
                .verified(true)
                .build();
    }

    @Test
    void getUser_shouldReturnCachedUser_whenPresent() {
        UserResponse cached = UserResponse.builder()
                .id(1L)
                .email("test@gmail.com")
                .build();

        when(repository.findEmailById(1L)).thenReturn(Optional.of("test@gmail.com"));
        when(userProfileCacheService.getCachedUser("test@gmail.com")).thenReturn(Optional.of(cached));

        UserResponse response = userService.getUser(1L);

        assertEquals(cached, response);
        verify(repository, never()).findById(1L);
    }

    @Test
    void getUser_shouldLoadAndCache_whenCacheMiss() {
        when(repository.findEmailById(1L)).thenReturn(Optional.of("test@gmail.com"));
        when(userProfileCacheService.getCachedUser("test@gmail.com")).thenReturn(Optional.empty());
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(encryptionService.decrypt("encrypted-phone")).thenReturn("9876543210");

        UserResponse response = userService.getUser(1L);

        assertEquals(1L, response.getId());
        assertEquals("Akash Sharma", response.getFullName());
        assertEquals("9876543210", response.getPhoneNumber());
        verify(userProfileCacheService).cacheUser(any(UserResponse.class));
    }

    @Test
    void getUser_shouldThrow_whenUserNotFound() {
        when(repository.findEmailById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUser(99L));
    }

    @Test
    void updateUser_shouldThrow_whenPhoneAlreadyRegistered() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Akash")
                .lastName("Sharma")
                .dob(LocalDate.of(2000, 1, 1))
                .gender(Gender.MALE)
                .phoneNumber("9123456789")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(phoneHashService.hash("9123456789")).thenReturn("new-hash");
        when(repository.existsByPhoneNumberHash("new-hash")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.updateUser(1L, request));
    }

    @Test
    void updateUser_shouldUpdateProfileAndEvictCache() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("New")
                .lastName("Name")
                .dob(LocalDate.of(1999, 5, 5))
                .gender(Gender.FEMALE)
                .phoneNumber("9123456789")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(phoneHashService.hash("9123456789")).thenReturn("new-hash");
        when(repository.existsByPhoneNumberHash("new-hash")).thenReturn(false);
        when(encryptionService.encrypt("9123456789")).thenReturn("new-encrypted");
        when(encryptionService.decrypt("new-encrypted")).thenReturn("9123456789");

        UserResponse response = userService.updateUser(1L, request);

        assertEquals("New", response.getFirstName());
        assertEquals("Name", response.getLastName());
        assertEquals("9123456789", response.getPhoneNumber());
        verify(userProfileCacheService).evictUser("test@gmail.com");
    }

    @Test
    void changePassword_shouldThrow_whenCurrentPasswordWrong() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("Wrong@123")
                .newPassword("NewPass@1")
                .confirmPassword("NewPass@1")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Wrong@123", "encoded-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.changePassword(1L, request));
    }

    @Test
    void changePassword_shouldThrow_whenConfirmDoesNotMatch() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("Password@1")
                .newPassword("NewPass@1")
                .confirmPassword("Mismatch@1")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@1", "encoded-password")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.changePassword(1L, request));
    }

    @Test
    void changePassword_shouldThrow_whenNewSameAsCurrent() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("Password@1")
                .newPassword("Password@1")
                .confirmPassword("Password@1")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@1", "encoded-password")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.changePassword(1L, request));
    }

    @Test
    void changePassword_shouldEncodeAndSetNewPassword() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("Password@1")
                .newPassword("NewPass@1")
                .confirmPassword("NewPass@1")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@1", "encoded-password")).thenReturn(true);
        when(passwordEncoder.encode("NewPass@1")).thenReturn("new-encoded");

        userService.changePassword(1L, request);

        assertEquals("new-encoded", user.getPassword());
    }

    @Test
    void deleteUser_shouldDeleteAndEvictCache() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(repository).delete(user);
        verify(userProfileCacheService).evictUser("test@gmail.com");
    }

    @Test
    void warmProfileCache_shouldCacheMappedResponse() {
        when(encryptionService.decrypt("encrypted-phone")).thenReturn("9876543210");

        userService.warmProfileCache(user);

        verify(userProfileCacheService).cacheUser(any(UserResponse.class));
    }
}
