package com.authentication.AuthProject.service;

import com.authentication.AuthProject.dto.LoginRequest;
import com.authentication.AuthProject.dto.SignupRequest;
import com.authentication.AuthProject.entity.User;
import com.authentication.AuthProject.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository repository;

    public AuthService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<String> signup(SignupRequest request) {

        if (request.hasMissingFields()) {
            return Optional.of("Please fill all required fields.");
        }

        if (repository.findByEmail(request.getEmail()).isPresent()) {
            return Optional.of("Email already registered.");
        }

        if (repository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            return Optional.of("Phone number already registered.");
        }

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDob(request.getDob());
        user.setGender(request.getGender());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(request.getPassword());

        repository.save(user);

        return Optional.empty();
    }

    public Optional<Long> login(LoginRequest request) {
        User user = repository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            return Optional.empty();
        }

        if (!user.getPassword().equals(request.getPassword())) {
            return Optional.empty();
        }

        return Optional.of(user.getId());
    }

    public Optional<String> getLoginError(LoginRequest request) {
        User user = repository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            return Optional.of("User not found.");
        }

        if (!user.getPassword().equals(request.getPassword())) {
            return Optional.of("Invalid password.");
        }

        return Optional.empty();
    }
}
