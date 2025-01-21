package com.fitnessapp.user.service;

import com.fitnessapp.exception.DomainException;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.model.UserRole;
import com.fitnessapp.user.repository.UserRepository;
import com.fitnessapp.web.dto.LoginRequest;
import com.fitnessapp.web.dto.RegisterRequest;
import com.fitnessapp.web.dto.UserEditRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @SuppressWarnings("StringConcatenationArgumentToLogCall")
    public User register(RegisterRequest registerRequest) {

        Optional<User> userOptional = userRepository.findByEmail(registerRequest.email());

        if (userOptional.isPresent()) {
            throw new DomainException("User with email [%s] already exist."
                    .formatted(registerRequest.email()), HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.save(initializeNewUserAccount(registerRequest));

        log.info("Successfully created new user for email [%s] with id [%s]."
                .formatted(user.getEmail(), user.getId()));

        return user;
    }

    public User login(LoginRequest loginRequest) {

        Optional<User> userOptional = userRepository.findByEmail(loginRequest.email());
        if (userOptional.isEmpty()) {
            throw new DomainException("User with email [%s] does not exist."
                    .formatted(loginRequest.email()), HttpStatus.BAD_REQUEST);
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new DomainException("Incorrect password.", HttpStatus.BAD_REQUEST);
        }

        return user;
    }

    public User updateUser(UUID userId, UserEditRequest userEditRequest) {

        User user = getById(userId);

        user.setFirstName(userEditRequest.firstName().trim());
        user.setLastName(userEditRequest.lastName().trim());

        return userRepository.save(user);
    }

    private User getById(UUID userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("User with id [%s] does not exist."
                        .formatted(userId), HttpStatus.BAD_REQUEST));
    }

    private User initializeNewUserAccount(RegisterRequest dto) {

        UserRole selectedRole = UserRole.valueOf(dto.userRole().name());

        if (!UserRole.getRegistrableRoles().contains(selectedRole)) {
            throw new IllegalArgumentException("Invalid role selected for registration");
        }

        return User.builder()
                .id(UUID.randomUUID())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .role(selectedRole)
                .build();
    }
}
