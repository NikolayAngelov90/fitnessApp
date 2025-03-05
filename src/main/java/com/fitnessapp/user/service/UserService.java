package com.fitnessapp.user.service;

import com.fitnessapp.exception.*;
import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.model.UserRole;
import com.fitnessapp.user.repository.UserRepository;
import com.fitnessapp.web.dto.RegisterRequest;
import com.fitnessapp.web.dto.UserEditRequest;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PhoneNumberService phoneNumberService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       PhoneNumberService phoneNumberService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.phoneNumberService = phoneNumberService;
    }

    public void register(RegisterRequest registerRequest) {

        Optional<User> userOptional = userRepository.findByEmail(registerRequest.email());

        if (userOptional.isPresent()) {
            throw new UserAlreadyExistsException("User with email [%s] already exist."
                    .formatted(registerRequest.email()));
        }

        User user = userRepository.save(initializeNewUserAccount(registerRequest));

        log.info("Successfully created new user for email [{}] with id [{}].",
                user.getEmail(), user.getId());
    }

    @Transactional
    public void updateUser(UUID userId, UserEditRequest userEditRequest) {

        User user = getById(userId);

        user.setFirstName(userEditRequest.firstName().trim());
        user.setLastName(userEditRequest.lastName().trim());

        String e164Number = getE164Number(userEditRequest);

        Optional<User> phoneNumberOptional = userRepository.findByPhoneNumber(e164Number);
        if (phoneNumberOptional.isPresent() && !phoneNumberOptional.get().getId().equals(user.getId())) {
            throw new PhoneNumberAlreadyExistsException("User with number [%s] already exist."
                    .formatted(e164Number));
        }

        user.setPhoneNumber(e164Number);
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

    public User findByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email [%s] does not exist."
                        .formatted(email)));
    }

    public User getById(UUID userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id [%s] does not exist."
                        .formatted(userId)));
    }

    public List<User> getAllTrainers() {

        List<User> trainers = userRepository.findByRole(UserRole.TRAINER);

        if (trainers.isEmpty()) {
            throw new TrainerNotFoundException("Trainer not found!");
        }

        return trainers;
    }

    private User initializeNewUserAccount(RegisterRequest dto) {

        UserRole selectedRole = UserRole.valueOf(dto.userRole().name());

        if (!UserRole.getRegistrableRoles().contains(selectedRole)) {
            throw new IllegalArgumentException("Invalid role selected for registration.");
        }

        return User.builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .role(selectedRole)
                .registeredOn(LocalDateTime.now())
                .build();
    }

    private void validateImage(MultipartFile profilePicture) {

        if (profilePicture.isEmpty()) {
            throw new EmptyImageException("Please choice profile picture.");
        }

        if (profilePicture.getSize() > 5 * 1024 * 1024) {
            throw new InvalidImageException("Size of profile picture must be less than 5MB.");
        }

        String contentType = profilePicture.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidImageException("Please upload a valid format of profile picture.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = findByEmail(email);

        log.info("Attempting to load user by email [{}].", email);

        return new CustomUserDetails(user.getId(), email, user.getPassword(), user.getRole());
    }

    private String getE164Number(UserEditRequest userEditRequest) {

        if (userEditRequest.phoneNumber().trim().isEmpty()) {
            return userEditRequest.phoneNumber();
        }

        PhoneNumber parsedNumber = phoneNumberService.parsePhoneNumber(
                userEditRequest.phoneNumber(), "BG");
        return phoneNumberService.formatE164(parsedNumber);
    }
}
