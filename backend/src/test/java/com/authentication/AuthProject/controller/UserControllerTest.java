package com.authentication.AuthProject.controller;

import com.authentication.AuthProject.dto.request.ChangePasswordRequest;
import com.authentication.AuthProject.dto.request.UpdateProfileRequest;
import com.authentication.AuthProject.dto.response.UserResponse;
import com.authentication.AuthProject.enums.Gender;
import com.authentication.AuthProject.exception.GlobalExceptionHandler;
import com.authentication.AuthProject.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .build();
    }

    @Test
    void getUser_shouldReturn200() throws Exception {
        when(userService.getUser(1L)).thenReturn(UserResponse.builder()
                .id(1L)
                .email("test@gmail.com")
                .firstName("Akash")
                .build());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@gmail.com"));
    }

    @Test
    void updateProfile_shouldReturn200() throws Exception {
        when(userService.updateUser(eq(1L), any(UpdateProfileRequest.class)))
                .thenReturn(UserResponse.builder()
                        .id(1L)
                        .firstName("New")
                        .lastName("Name")
                        .gender(Gender.MALE)
                        .dob(LocalDate.of(2000, 1, 1))
                        .phoneNumber("9876543210")
                        .email("test@gmail.com")
                        .build());

        String body = """
                {
                  "firstName": "New",
                  "lastName": "Name",
                  "dob": "2000-01-01",
                  "gender": "MALE",
                  "phoneNumber": "9876543210"
                }
                """;

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("New"));
    }

    @Test
    void changePassword_shouldReturn204() throws Exception {
        doNothing().when(userService).changePassword(eq(1L), any(ChangePasswordRequest.class));

        String body = """
                {
                  "currentPassword": "Password@1",
                  "newPassword": "NewPass@1",
                  "confirmPassword": "NewPass@1"
                }
                """;

        mockMvc.perform(put("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(eq(1L), any(ChangePasswordRequest.class));
    }
}
