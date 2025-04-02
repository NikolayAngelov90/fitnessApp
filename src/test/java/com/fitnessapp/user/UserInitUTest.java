package com.fitnessapp.user;

import com.fitnessapp.user.model.User;
import com.fitnessapp.user.model.UserRole;
import com.fitnessapp.user.repository.UserRepository;
import com.fitnessapp.user.service.UserInit;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.model.WorkoutStatus;
import com.fitnessapp.workout.model.WorkoutType;
import com.fitnessapp.workout.service.WorkoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserInitUTest {

    private final String encodedAdminPassword = "encodedAdminPassword123";
    private final String encodedTrainerPassword = "encodedTrainerPassword123";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private WorkoutService workoutService;
    @Captor
    private ArgumentCaptor<User> userCaptor;
    @Captor
    private ArgumentCaptor<Workout> workoutCaptor;

    @InjectMocks
    private UserInit userInit;

    @BeforeEach
    void setUp() {

        lenient().when(passwordEncoder.encode(eq("ADMIN123"))).thenReturn(encodedAdminPassword);
        lenient().when(passwordEncoder.encode(eq("TRAINER123"))).thenReturn(encodedTrainerPassword);

        lenient().when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }


    @Test
    void givenNoUsersExist_whenRun_thenCreateAdminAndTrainerAndWorkout() {
        // Given
        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.empty());

        // When
        userInit.run();

        // Then
        verify(passwordEncoder).encode("ADMIN123");
        verify(passwordEncoder).encode("TRAINER123");

        verify(userRepository, times(2)).save(userCaptor.capture());
        List<User> savedUsers = userCaptor.getAllValues();
        User savedAdmin = savedUsers.get(0);
        User savedTrainer = savedUsers.get(1);

        assertEquals("admin@gmail.com", savedAdmin.getEmail());
        assertEquals(encodedAdminPassword, savedAdmin.getPassword());
        assertEquals(UserRole.ADMIN, savedAdmin.getRole());
        assertNotNull(savedAdmin.getRegisteredOn());

        assertEquals("trainer@gmail.com", savedTrainer.getEmail());
        assertEquals(encodedTrainerPassword, savedTrainer.getPassword());
        assertEquals(UserRole.TRAINER, savedTrainer.getRole());
        assertEquals("+35921234567", savedTrainer.getPhoneNumber());
        assertEquals(UserInit.TRAINER_SPECIALIZATION, savedTrainer.getSpecialization());
        assertEquals(UserInit.TRAINER_DESCRIPTION, savedTrainer.getDescription());
        assertTrue(savedTrainer.isAdditionalTrainerDataCompleted());
        assertTrue(savedTrainer.isApproveTrainer());
        assertNotNull(savedTrainer.getRegisteredOn());

        verify(workoutService).saveDefaultTrainerWorkout(workoutCaptor.capture());
        Workout savedWorkout = workoutCaptor.getValue();

        assertNotNull(savedWorkout);
        assertEquals(savedTrainer, savedWorkout.getTrainer());
        assertEquals(UserInit.WORKOUT_DESCRIPTION, savedWorkout.getDescription());
        assertEquals(BigDecimal.valueOf(30), savedWorkout.getPrice());
        assertEquals(40, savedWorkout.getDuration());
        assertEquals(WorkoutType.CROSS_FIT, savedWorkout.getWorkoutType());
        assertEquals(WorkoutStatus.UPCOMING, savedWorkout.getStatus());
        assertNotNull(savedWorkout.getCreatedAt());
        assertNotNull(savedWorkout.getStartTime());
    }

    @Test
    void givenAdminExistsTrainerDoesNotExist_whenRun_thenCreateTrainerAndWorkoutOnly() {
        // Given
        User existingAdmin = User.builder().email("admin@gmail.com").role(UserRole.ADMIN).build();
        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(existingAdmin));
        when(userRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.empty());

        // When
        userInit.run();

        // Then
        verify(passwordEncoder, never()).encode("ADMIN123");

        verify(passwordEncoder).encode("TRAINER123");
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedTrainer = userCaptor.getValue();

        assertEquals("trainer@gmail.com", savedTrainer.getEmail());
        assertEquals(UserRole.TRAINER, savedTrainer.getRole());

        verify(workoutService).saveDefaultTrainerWorkout(workoutCaptor.capture());
        Workout savedWorkout = workoutCaptor.getValue();
        assertNotNull(savedWorkout);
        assertEquals(savedTrainer, savedWorkout.getTrainer());
    }

    @Test
    void givenTrainerExistsAdminDoesNotExist_whenRun_thenCreateAdminOnly() {
        // Given
        User existingTrainer = User.builder().email("trainer@gmail.com").role(UserRole.TRAINER).build();
        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(existingTrainer));

        // When
        userInit.run();

        // Then
        verify(passwordEncoder).encode("ADMIN123");
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedAdmin = userCaptor.getValue();
        assertEquals("admin@gmail.com", savedAdmin.getEmail());
        assertEquals(UserRole.ADMIN, savedAdmin.getRole());

        verify(passwordEncoder, never()).encode("TRAINER123");
        verify(workoutService, never()).saveDefaultTrainerWorkout(any(Workout.class));
    }

    @Test
    void givenBothUsersExist_whenRun_thenDoNothing() {
        // Given
        User existingAdmin = User.builder().email("admin@gmail.com").role(UserRole.ADMIN).build();
        User existingTrainer = User.builder().email("trainer@gmail.com").role(UserRole.TRAINER).build();
        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(existingAdmin));
        when(userRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(existingTrainer));

        // When
        userInit.run();

        // Then
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(workoutService, never()).saveDefaultTrainerWorkout(any(Workout.class));
    }
}
