package com.fitnessapp.user.service;

import com.fitnessapp.exception.*;
import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.model.UserRole;
import com.fitnessapp.user.repository.UserRepository;
import com.fitnessapp.web.dto.RegisterRequest;
import com.fitnessapp.web.dto.SwitchUserRoleRequest;
import com.fitnessapp.web.dto.UserEditRequest;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    @CacheEvict(value = "trainers", allEntries = true)
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

    @CacheEvict(value = "trainers", allEntries = true)
    public void updateUser(UUID userId, UserEditRequest userEditRequest) {

        User user = getById(userId);

        user.setFirstName(userEditRequest.firstName().trim());
        user.setLastName(userEditRequest.lastName().trim());

        if (userEditRequest.phoneNumber() != null && !userEditRequest.phoneNumber().trim().isEmpty()) {

            String e164Number = getE164Number(userEditRequest);
            Optional<User> phoneNumberOptional = userRepository.findByPhoneNumber(e164Number);
            if (phoneNumberOptional.isPresent() && !phoneNumberOptional.get().getId().equals(user.getId())) {
                throw new PhoneNumberAlreadyExistsException("User with number [%s] already exist."
                        .formatted(e164Number));
            }

            user.setPhoneNumber(e164Number.trim());
        }

        if (user.getRole() == UserRole.TRAINER) {
            user.setSpecialization(userEditRequest.specialization().trim().replaceAll("\\s+", " "));
            user.setDescription(userEditRequest.description().trim().replaceAll("\\s+", " "));
            user.setAdditionalTrainerDataCompleted(true);
        }

        userRepository.save(user);
        log.info("Successfully updated user {} with id [{}].", user.getEmail(), user.getId());
    }

    @CacheEvict(value = "trainers", allEntries = true)
    public void uploadProfilePicture(UUID userId, MultipartFile profilePicture) {

        User user = getById(userId);
        validateImage(profilePicture);

        byte[] compressedImage = compressImage(profilePicture);
        user.setProfilePicture(compressedImage);

        userRepository.save(user);
        log.info("Successfully uploaded profile picture to user [{}] with id [{}].", user.getEmail(), user.getId());
    }

    @CacheEvict(value = "trainers", allEntries = true)
    public void deleteProfilePicture(UUID userId) {

        User user = getById(userId);
        user.setProfilePicture(null);

        userRepository.save(user);
        log.info("Successfully delete profile pictures to user [{}] with id [{}].", user.getEmail(), user.getId());
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

    @Cacheable("trainers")
    public List<User> getAllApprovedTrainers() {

        List<User> approvedTrainers = userRepository.findByRoleAndApproveTrainer(UserRole.TRAINER, true);

        if (approvedTrainers.isEmpty()) {
            throw new TrainerNotFoundException("Trainer not found!");
        }

        return approvedTrainers;
    }

    public List<User> getPendingApproveTrainers() {
        return userRepository.findByRoleAndApproveTrainer(UserRole.TRAINER, false);
    }

    @CacheEvict(value = "trainers", allEntries = true)
    public void approveTrainer(UUID id) {
        User trainer = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User with id [%s] does not exist.".formatted(id)));

        trainer.setApproveTrainer(true);

        userRepository.save(trainer);
        log.info("Successfully approved trainer [{}] with id [{}].", trainer.getEmail(), trainer.getId());
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @CacheEvict(value = "trainers", allEntries = true)
    public void switchRole(UUID id, SwitchUserRoleRequest switchUserRoleRequest) {
        User user = getById(id);
        user.setRole(switchUserRoleRequest.userRole());

        userRepository.save(user);
        log.info("User [{}] with id [{}] has been switched role to [{}].", user.getEmail(), user.getId(), user.getRole());
    }

    private byte[] compressImage(MultipartFile profilePicture) {

        try (InputStream inputStream = profilePicture.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Thumbnails.of(inputStream)
                    .size(300, 300)
                    .outputFormat("jpg")
                    .outputQuality(0.7)
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new ImageUploadException("Error compressing image.");
        }
    }

    private User initializeNewUserAccount(RegisterRequest dto) {

        UserRole selectedRole = UserRole.valueOf(dto.userRole().name());

        if (!UserRole.getRegistrableRoles().contains(selectedRole)) {
            throw new IllegalArgumentException("Invalid role selected for registration.");
        }

        return User.builder()
                .email(dto.email().trim())
                .password(passwordEncoder.encode(dto.password()))
                .role(selectedRole)
                .registeredOn(LocalDateTime.now())
                .build();
    }

    private void validateImage(MultipartFile profilePicture) {

        if (profilePicture.isEmpty()) {
            throw new EmptyImageException("Please choice profile picture.");
        }

        if (profilePicture.getSize() > 10 * 1024 * 1024) {
            throw new InvalidImageException("Size of profile picture must be less than 10MB.");
        }

        String contentType = profilePicture.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidImageException("Please upload a valid format of profile picture.");
        }
    }

    private String getE164Number(UserEditRequest userEditRequest) {

        PhoneNumber parsedNumber = phoneNumberService.parsePhoneNumber(
                userEditRequest.phoneNumber(), "BG");
        return phoneNumberService.formatE164(parsedNumber);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = findByEmail(email);

        log.info("Attempting to load user by email [{}].", email);

        return new CustomUserDetails(
                user.getId(),
                email,
                user.getPassword(),
                user.getRole(),
                user.isAdditionalTrainerDataCompleted());
    }
}
