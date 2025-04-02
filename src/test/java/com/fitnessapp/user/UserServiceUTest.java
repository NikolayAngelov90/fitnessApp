package com.fitnessapp.user;

import com.fitnessapp.exception.*;
import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.model.UserRole;
import com.fitnessapp.user.repository.UserRepository;
import com.fitnessapp.user.service.PhoneNumberService;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.web.dto.RegisterRequest;
import com.fitnessapp.web.dto.SwitchUserRoleRequest;
import com.fitnessapp.web.dto.UserEditRequest;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PhoneNumberService phoneNumberService;
    @Captor
    private ArgumentCaptor<User> userCaptor;

    @InjectMocks
    private UserService userService;

    @Test
    void givenValidRegisterRequest_whenRegister_thenUserCreated() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest(
                "test@example.com",
                "password123",
                UserRole.CLIENT
        );

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        // When
        userService.register(registerRequest);

        // Then
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals(UserRole.CLIENT, savedUser.getRole());
        assertNotNull(savedUser.getRegisteredOn());
    }

    @Test
    void givenExistingEmail_whenRegister_thenThrowException() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest(
                "existing@example.com",
                "password123",
                UserRole.CLIENT
        );

        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email("existing@example.com")
                .build();

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> userService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void givenInvalidRole_whenRegister_thenThrowException() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest(
                "test@example.com",
                "password123",
                UserRole.ADMIN
        );

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void givenValidEditRequest_whenUpdateUser_thenUserUpdated() {
        // Given
        UUID userId = UUID.randomUUID();
        UserEditRequest editRequest = new UserEditRequest(
                "John",
                "Doe",
                "+359888123456",
                "Fitness",
                "Experienced trainer"
        );

        User existingUser = User.builder()
                .id(userId)
                .email("john@example.com")
                .role(UserRole.TRAINER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(phoneNumberService.parsePhoneNumber(anyString(), anyString())).thenReturn(new PhoneNumber());
        when(phoneNumberService.formatE164(any())).thenReturn("+359888123456");
        when(userRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());

        // When
        userService.updateUser(userId, editRequest);

        // Then
        verify(userRepository).save(userCaptor.capture());
        User updatedUser = userCaptor.getValue();

        assertEquals("John", updatedUser.getFirstName());
        assertEquals("Doe", updatedUser.getLastName());
        assertEquals("+359888123456", updatedUser.getPhoneNumber());
        assertEquals("Fitness", updatedUser.getSpecialization());
        assertEquals("Experienced trainer", updatedUser.getDescription());
        assertTrue(updatedUser.isAdditionalTrainerDataCompleted());
    }

    @Test
    void givenExistingPhoneNumber_whenUpdateUser_thenThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UserEditRequest editRequest = new UserEditRequest(
                "John",
                "Doe",
                "+359888123456",
                "Fitness",
                "Experienced trainer"
        );

        User existingUser = User.builder()
                .id(userId)
                .email("john@example.com")
                .role(UserRole.TRAINER)
                .build();

        User otherUser = User.builder()
                .id(otherUserId)
                .phoneNumber("+359888123456")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(phoneNumberService.parsePhoneNumber(anyString(), anyString())).thenReturn(new PhoneNumber());
        when(phoneNumberService.formatE164(any())).thenReturn("+359888123456");
        when(userRepository.findByPhoneNumber("+359888123456")).thenReturn(Optional.of(otherUser));

        // When & Then
        assertThrows(PhoneNumberAlreadyExistsException.class, () -> userService.updateUser(userId, editRequest));
    }

    @Test
    void givenValidImageFile_whenUploadProfilePicture_thenImageSaved() throws IOException {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("john@example.com")
                .build();

        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", byteArray);
        byte[] imageBytes = byteArray.toByteArray();

        MockMultipartFile profilePicture = new MockMultipartFile(
                "image.jpg", "image.jpg", "image/jpeg", imageBytes);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.uploadProfilePicture(userId, profilePicture);

        // Then
        verify(userRepository).save(userCaptor.capture());
        User updatedUser = userCaptor.getValue();

        assertNotNull(updatedUser.getProfilePicture());
    }

    @Test
    void givenEmptyImageFile_whenUploadProfilePicture_thenThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("john@example.com")
                .build();

        MockMultipartFile profilePicture = new MockMultipartFile(
                "image.jpg",
                "image.jpg",
                "image/jpeg",
                new byte[0]
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(EmptyImageException.class, () -> userService.uploadProfilePicture(userId, profilePicture));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void givenInvalidImageType_whenUploadProfilePicture_thenThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("john@example.com")
                .build();

        MockMultipartFile profilePicture = new MockMultipartFile(
                "document.pdf",
                "document.pdf",
                "application/pdf",
                "test document content".getBytes());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(InvalidImageException.class, () -> userService.uploadProfilePicture(userId, profilePicture));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void givenUserId_whenDeleteProfilePicture_thenPictureRemoved() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("john@example.com")
                .profilePicture("previous image".getBytes())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.deleteProfilePicture(userId);

        // Then
        verify(userRepository).save(userCaptor.capture());
        User updatedUser = userCaptor.getValue();

        assertNull(updatedUser.getProfilePicture());
    }

    @Test
    void givenExistingEmail_whenFindByEmail_thenReturnUser() {
        // Given
        String email = "test@example.com";
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        User foundUser = userService.findByEmail(email);

        // Then
        assertEquals(user, foundUser);
    }

    @Test
    void givenNonExistentEmail_whenFindByEmail_thenThrowException() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.findByEmail(email));
    }

    @Test
    void givenExistingId_whenGetById_thenReturnUser() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        User foundUser = userService.getById(userId);

        // Then
        assertEquals(user, foundUser);
    }

    @Test
    void givenNonExistentId_whenGetById_thenThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.getById(userId));
    }

    @Test
    void givenApprovedTrainers_whenGetAllApprovedTrainers_thenReturnList() {
        // Given
        List<User> trainers = Arrays.asList(
                User.builder().id(UUID.randomUUID()).role(UserRole.TRAINER).approveTrainer(true).build(),
                User.builder().id(UUID.randomUUID()).role(UserRole.TRAINER).approveTrainer(true).build()
        );

        when(userRepository.findByRoleAndApproveTrainer(UserRole.TRAINER, true)).thenReturn(trainers);

        // When
        List<User> result = userService.getAllApprovedTrainers();

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void givenNoApprovedTrainers_whenGetAllApprovedTrainers_thenThrowException() {
        // Given
        when(userRepository.findByRoleAndApproveTrainer(UserRole.TRAINER, true)).thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(TrainerNotFoundException.class, () -> userService.getAllApprovedTrainers());
    }

    @Test
    void givenPendingTrainers_whenGetPendingApproveTrainers_thenReturnList() {
        // Given
        List<User> trainers = Arrays.asList(
                User.builder().id(UUID.randomUUID()).role(UserRole.TRAINER).approveTrainer(false).build(),
                User.builder().id(UUID.randomUUID()).role(UserRole.TRAINER).approveTrainer(false).build()
        );

        when(userRepository.findByRoleAndApproveTrainer(UserRole.TRAINER, false)).thenReturn(trainers);

        // When
        List<User> result = userService.getPendingApproveTrainers();

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void givenTrainerId_whenApproveTrainer_thenTrainerApproved() {
        // Given
        UUID trainerId = UUID.randomUUID();
        User trainer = User.builder()
                .id(trainerId)
                .email("trainer@example.com")
                .role(UserRole.TRAINER)
                .approveTrainer(false)
                .build();

        when(userRepository.findById(trainerId)).thenReturn(Optional.of(trainer));

        // When
        userService.approveTrainer(trainerId);

        // Then
        verify(userRepository).save(userCaptor.capture());
        User updatedTrainer = userCaptor.getValue();

        assertTrue(updatedTrainer.isApproveTrainer());
    }

    @Test
    void givenUserId_whenSwitchRole_thenRoleChanged() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("user@example.com")
                .role(UserRole.CLIENT)
                .build();

        SwitchUserRoleRequest request = new SwitchUserRoleRequest(UserRole.TRAINER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.switchRole(userId, request);

        // Then
        verify(userRepository).save(userCaptor.capture());
        User updatedUser = userCaptor.getValue();

        assertEquals(UserRole.TRAINER, updatedUser.getRole());
    }

    @Test
    void givenValidEmail_whenLoadUserByUsername_thenReturnUserDetails() {
        // Given
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .email(email)
                .password("encodedPassword")
                .role(UserRole.CLIENT)
                .additionalTrainerDataCompleted(false)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = userService.loadUserByUsername(email);

        // Then
        assertInstanceOf(CustomUserDetails.class, userDetails);
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;

        assertEquals(userId, customUserDetails.getUserId());
        assertEquals(email, customUserDetails.getUsername());
        assertEquals("encodedPassword", customUserDetails.getPassword());
        assertEquals(UserRole.CLIENT, customUserDetails.getRole());
        assertFalse(customUserDetails.isAdditionalTrainerDataCompleted());
    }

    @Test
    void givenNonExistentEmail_whenLoadUserByUsername_thenThrowException() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.loadUserByUsername(email));
    }
}
