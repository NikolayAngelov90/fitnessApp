package com.fitnessapp.user.service;

import com.fitnessapp.exception.*;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.model.UserRole;
import com.fitnessapp.user.repository.UserRepository;
import com.fitnessapp.web.dto.RegisterRequest;
import com.fitnessapp.web.dto.UserEditRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
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
    public void register(RegisterRequest registerRequest) {

        Optional<User> userOptional = userRepository.findByEmail(registerRequest.email());

        if (userOptional.isPresent()) {
            throw new UserAlreadyExistsException("User with email (%s) already exist."
                    .formatted(registerRequest.email()));
        }

        User user = userRepository.save(initializeNewUserAccount(registerRequest));

        log.info("Successfully created new user for email [%s] with id [%s]."
                .formatted(user.getEmail(), user.getId()));

    }

    public void updateUser(UUID userId, UserEditRequest userEditRequest) {

        User user = getById(userId);

        user.setFirstName(userEditRequest.firstName().trim());
        user.setLastName(userEditRequest.lastName().trim());

        userRepository.save(user);
    }

    public void uploadProfilePicture(UUID userId, MultipartFile profilePicture) {

        User user = getById(userId);

        validateImage(profilePicture);

        try {
            user.setProfilePicture(profilePicture.getBytes());
            userRepository.save(user);
        } catch (IOException e) {
            throw new ImageUploadException("Error processing photo.");
        }
    }

    private void validateImage(MultipartFile profilePicture) {

        if (profilePicture.isEmpty()) {
            throw new EmptyImageException();
        }

        if (profilePicture.getSize() > 5 * 1024 * 1024) {
            throw new InvalidImageException("Size of profile picture must be less than 5MB.");
        }

        String contentType = profilePicture.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidImageException("Please upload a valid format of profile picture.");
        }
    }

    public User getById(UUID userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id (%s) does not exist."
                        .formatted(userId)));
    }


    public User findByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email (%s) does not exist."));
    }

    private User initializeNewUserAccount(RegisterRequest dto) {

        UserRole selectedRole = UserRole.valueOf(dto.userRole().name());

        if (!UserRole.getRegistrableRoles().contains(selectedRole)) {
            throw new IllegalArgumentException("Invalid role selected for registration");
        }

        return User.builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .role(selectedRole)
                .registeredOn(LocalDateTime.now())
                .build();
    }
}
