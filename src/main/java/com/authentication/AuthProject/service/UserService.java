package com.authentication.AuthProject.service;

import com.authentication.AuthProject.dto.request.ChangePasswordRequest;
import com.authentication.AuthProject.dto.request.UpdateProfileRequest;
import com.authentication.AuthProject.dto.response.UserResponse;
import com.authentication.AuthProject.entity.User;
import com.authentication.AuthProject.repository.UserRepository;
import com.authentication.AuthProject.util.AgeCalculator;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<UserResponse> getUser(Long id) {
        return repository.findById(id).map(this::toResponse);
    }

    public Optional<String> updateUser(Long id, UpdateProfileRequest request) {
        Optional<User> userOptional = repository.findById(id);

        if (userOptional.isEmpty()) {
            return Optional.of("User not found.");
        }

        User user = userOptional.get();

        //NEed to be checked  ...!!!!!!!!!!!!!!1

        if (!user.getPhoneNumber().equals(request.getPhoneNumber())
                && repository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            return Optional.of("Phone number already registered.");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDob(request.getDob());
        user.setGender(request.getGender());
        user.setPhoneNumber(request.getPhoneNumber());

        repository.save(user);

        //auto update the age accordingly


        return Optional.empty();
    }

    public Optional<String> changePassword(Long id, ChangePasswordRequest request) {
        Optional<User> userOptional = repository.findById(id);

        if (userOptional.isEmpty()) {
            return Optional.of("User not found.");
        }

        User user = userOptional.get();

        if (!user.getPassword().equals(request.getCurrentPassword())) {
            return Optional.of("Current password is incorrect.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return Optional.of("New password and confirm password do not match.");
        }

        user.setPassword(request.getNewPassword());

        //use sql query for updation

        repository.save(user);

        return Optional.empty();
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFirstName() + " " + user.getLastName());
        response.setDob(user.getDob());
        response.setAge(AgeCalculator.calculate(user.getDob()));
        response.setGender(user.getGender());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());

        return response;
    }
}
