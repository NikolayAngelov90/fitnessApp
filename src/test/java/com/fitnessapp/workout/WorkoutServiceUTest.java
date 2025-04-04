package com.fitnessapp.workout;

import com.fitnessapp.exception.*;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.model.UserRole;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.web.dto.WorkoutRequest;
import com.fitnessapp.workout.model.RecurringType;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.model.WorkoutStatus;
import com.fitnessapp.workout.model.WorkoutType;
import com.fitnessapp.workout.property.WorkoutProperty;
import com.fitnessapp.workout.repository.WorkoutRepository;
import com.fitnessapp.workout.service.WorkoutService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkoutServiceUTest {

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private UserService userService;

    @Mock
    private WorkoutProperty workoutProperty;

    @Captor
    private ArgumentCaptor<Workout> workoutCaptor;

    @InjectMocks
    private WorkoutService workoutService;

    @Test
    void givenValidRequest_whenCreateWorkout_thenWorkoutCreated() {
        // Given
        WorkoutRequest workoutRequest = new WorkoutRequest(
                WorkoutType.YOGA,
                60,
                BigDecimal.valueOf(20),
                LocalDateTime.now().plusDays(1),
                RecurringType.NONE,
                "Yoga for beginners",
                10
        );

        UUID trainerId = UUID.randomUUID();
        User trainer = User.builder()
                .id(trainerId)
                .email("trainer@example.com")
                .role(UserRole.TRAINER)
                .approveTrainer(true)
                .build();

        when(userService.getById(trainerId)).thenReturn(trainer);
        when(workoutProperty.getDefaultStatus()).thenReturn(WorkoutStatus.UPCOMING);

        // When
        workoutService.create(workoutRequest, trainerId);

        // Then
        verify(workoutRepository).save(workoutCaptor.capture());
        Workout savedWorkout = workoutCaptor.getValue();

        assertEquals(WorkoutType.YOGA, savedWorkout.getWorkoutType());
        assertEquals(60, savedWorkout.getDuration());
        assertEquals(BigDecimal.valueOf(20), savedWorkout.getPrice());
        assertEquals(workoutRequest.startTime(), savedWorkout.getStartTime());
        assertEquals(workoutRequest.startTime().plusMinutes(60), savedWorkout.getEndTime());
        assertEquals(RecurringType.NONE, savedWorkout.getRecurringType());
        assertEquals(trainer, savedWorkout.getTrainer());
        assertEquals("Yoga for beginners", savedWorkout.getDescription());
        assertEquals(10, savedWorkout.getMaxParticipants());
        assertEquals(10, savedWorkout.getAvailableSpots());
        assertEquals(WorkoutStatus.UPCOMING, savedWorkout.getStatus());
    }

    @Test
    void givenNonApprovedTrainer_whenCreateWorkout_thenThrowException() {
        // Given
        WorkoutRequest workoutRequest = new WorkoutRequest(
                WorkoutType.YOGA,
                60,
                BigDecimal.valueOf(20),
                LocalDateTime.now().plusDays(1),
                RecurringType.NONE,
                "Yoga for beginners",
                10
        );

        UUID trainerId = UUID.randomUUID();
        User trainer = User.builder()
                .id(trainerId)
                .email("trainer@example.com")
                .role(UserRole.TRAINER)
                .approveTrainer(false)
                .build();

        when(userService.getById(trainerId)).thenReturn(trainer);

        // When & Then
        assertThrows(TrainerNotApproveException.class, () -> workoutService.create(workoutRequest, trainerId));
        verify(workoutRepository, never()).save(any(Workout.class));
    }

    @Test
    void givenValidRegistration_whenRegisterClient_thenClientRegistered() {
        // Given
        Workout workout = Workout.builder()
                .id(UUID.randomUUID())
                .status(WorkoutStatus.UPCOMING)
                .availableSpots(5)
                .maxParticipants(10)
                .clients(new ArrayList<>())
                .build();

        User client = User.builder()
                .id(UUID.randomUUID())
                .email("client@example.com")
                .role(UserRole.CLIENT)
                .build();

        // When
        workoutService.registerClient(workout, client);

        // Then
        verify(workoutRepository).save(workoutCaptor.capture());
        Workout updatedWorkout = workoutCaptor.getValue();

        assertTrue(updatedWorkout.getClients().contains(client));
        assertEquals(4, updatedWorkout.getAvailableSpots());
        assertEquals(WorkoutStatus.UPCOMING, updatedWorkout.getStatus());
    }

    @Test
    void givenLastSpotRegistration_whenRegisterClient_thenWorkoutStatusChangedToFull() {
        // Given
        Workout workout = Workout.builder()
                .id(UUID.randomUUID())
                .status(WorkoutStatus.UPCOMING)
                .availableSpots(1)
                .maxParticipants(10)
                .clients(new ArrayList<>())
                .build();

        User client = User.builder()
                .id(UUID.randomUUID())
                .email("client@example.com")
                .role(UserRole.CLIENT)
                .build();

        // When
        workoutService.registerClient(workout, client);

        // Then
        verify(workoutRepository).save(workoutCaptor.capture());
        Workout updatedWorkout = workoutCaptor.getValue();

        assertTrue(updatedWorkout.getClients().contains(client));
        assertEquals(0, updatedWorkout.getAvailableSpots());
        assertEquals(WorkoutStatus.FULL, updatedWorkout.getStatus());
    }

    @Test
    void givenFullWorkout_whenRegisterClient_thenThrowException() {
        // Given
        Workout workout = Workout.builder()
                .id(UUID.randomUUID())
                .status(WorkoutStatus.FULL)
                .availableSpots(0)
                .maxParticipants(10)
                .clients(new ArrayList<>())
                .build();

        User client = User.builder()
                .id(UUID.randomUUID())
                .email("client@example.com")
                .role(UserRole.CLIENT)
                .build();

        // When & Then
        assertThrows(WorkoutFullException.class, () -> workoutService.registerClient(workout, client));
        verify(workoutRepository, never()).save(any(Workout.class));
    }

    @Test
    void givenAlreadyRegisteredClient_whenRegisterClient_thenThrowException() {
        // Given
        User client = User.builder()
                .id(UUID.randomUUID())
                .email("client@example.com")
                .role(UserRole.CLIENT)
                .build();

        Workout workout = Workout.builder()
                .id(UUID.randomUUID())
                .status(WorkoutStatus.UPCOMING)
                .availableSpots(5)
                .maxParticipants(10)
                .clients(new ArrayList<>(List.of(client)))
                .build();

        // When & Then
        assertThrows(DuplicateRegistrationClientWorkout.class, () -> workoutService.registerClient(workout, client));
        verify(workoutRepository, never()).save(any(Workout.class));
    }

    @Test
    void givenValidCancellation_whenCancelBookingWorkout_thenBookingCancelled() {
        // Given
        UUID clientId = UUID.randomUUID();
        User client = User.builder()
                .id(clientId)
                .email("client@example.com")
                .role(UserRole.CLIENT)
                .build();

        UUID workoutId = UUID.randomUUID();
        Workout workout = Workout.builder()
                .id(workoutId)
                .status(WorkoutStatus.UPCOMING)
                .availableSpots(4)
                .maxParticipants(10)
                .clients(new ArrayList<>(List.of(client)))
                .build();

        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
        when(userService.getById(clientId)).thenReturn(client);

        // When
        workoutService.cancelBookingWorkout(workoutId, clientId);

        // Then
        verify(workoutRepository).save(workoutCaptor.capture());
        Workout updatedWorkout = workoutCaptor.getValue();

        assertFalse(updatedWorkout.getClients().contains(client));
        assertEquals(5, updatedWorkout.getAvailableSpots());
    }

    @Test
    void givenCompletedWorkout_whenCancelBookingWorkout_thenThrowException() {
        // Given
        UUID clientId = UUID.randomUUID();
        User client = User.builder()
                .id(clientId)
                .email("client@example.com")
                .role(UserRole.CLIENT)
                .build();

        UUID workoutId = UUID.randomUUID();
        Workout workout = Workout.builder()
                .id(workoutId)
                .status(WorkoutStatus.COMPLETED)
                .availableSpots(4)
                .maxParticipants(10)
                .clients(new ArrayList<>(List.of(client)))
                .build();

        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
        when(userService.getById(clientId)).thenReturn(client);

        // When & Then
        assertThrows(CancelBookedWorkoutException.class, () -> workoutService.cancelBookingWorkout(workoutId, clientId));
        verify(workoutRepository, never()).save(any(Workout.class));
    }

    @Test
    void givenClientId_whenGetAllRegisteredClientWorkouts_thenReturnWorkouts() {
        // Given
        UUID clientId = UUID.randomUUID();
        User client = User.builder()
                .id(clientId)
                .email("client@example.com")
                .role(UserRole.CLIENT)
                .build();

        List<Workout> workouts = Arrays.asList(
                Workout.builder().id(UUID.randomUUID()).build(),
                Workout.builder().id(UUID.randomUUID()).build()
        );

        when(userService.getById(clientId)).thenReturn(client);
        when(workoutRepository.findAllByClientsOrderByEndTimeDesc(List.of(client))).thenReturn(workouts);

        // When
        List<Workout> result = workoutService.getAllRegisteredClientWorkouts(clientId);

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void givenWorkoutId_whenGetById_thenReturnWorkout() {
        // Given
        UUID workoutId = UUID.randomUUID();
        Workout workout = Workout.builder()
                .id(workoutId)
                .build();

        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));

        // When
        Workout result = workoutService.getById(workoutId);

        // Then
        assertEquals(workout, result);
    }

    @Test
    void givenNonExistentWorkoutId_whenGetById_thenThrowException() {
        // Given
        UUID workoutId = UUID.randomUUID();
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(WorkoutNotFoundException.class, () -> workoutService.getById(workoutId));
    }

    @Test
    void givenValidRequest_whenEditWorkout_thenWorkoutUpdated() {
        // Given
        Workout workout = Workout.builder()
                .id(UUID.randomUUID())
                .workoutType(WorkoutType.YOGA)
                .duration(60)
                .price(BigDecimal.valueOf(20))
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(60))
                .recurringType(RecurringType.NONE)
                .description("Yoga for beginners")
                .maxParticipants(10)
                .availableSpots(5)
                .clients(new ArrayList<>())
                .build();

        WorkoutRequest workoutRequest = new WorkoutRequest(
                WorkoutType.PILATES,
                90,
                BigDecimal.valueOf(25),
                LocalDateTime.now().plusDays(2),
                RecurringType.WEEKLY,
                "Pilates for intermediates",
                15
        );

        // When
        workoutService.edit(workout, workoutRequest);

        // Then
        verify(workoutRepository).save(workoutCaptor.capture());
        Workout updatedWorkout = workoutCaptor.getValue();

        assertEquals(WorkoutType.PILATES, updatedWorkout.getWorkoutType());
        assertEquals(90, updatedWorkout.getDuration());
        assertEquals(BigDecimal.valueOf(25), updatedWorkout.getPrice());
        assertEquals(workoutRequest.startTime(), updatedWorkout.getStartTime());
        assertEquals(workoutRequest.startTime().plusMinutes(90), updatedWorkout.getEndTime());
        assertEquals(RecurringType.WEEKLY, updatedWorkout.getRecurringType());
        assertEquals("Pilates for intermediates", updatedWorkout.getDescription());
        assertEquals(15, updatedWorkout.getMaxParticipants());
        assertEquals(10, updatedWorkout.getAvailableSpots());
    }

    @Test
    void givenReducedMaxParticipants_whenEditWorkout_thenAvailableSpotsAdjusted() {
        // Given
        Workout workout = Workout.builder()
                .id(UUID.randomUUID())
                .workoutType(WorkoutType.YOGA)
                .duration(60)
                .price(BigDecimal.valueOf(20))
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(60))
                .recurringType(RecurringType.NONE)
                .description("Yoga for beginners")
                .maxParticipants(10)
                .availableSpots(8)
                .clients(new ArrayList<>())
                .build();

        WorkoutRequest workoutRequest = new WorkoutRequest(
                WorkoutType.YOGA,
                60,
                BigDecimal.valueOf(20),
                LocalDateTime.now().plusDays(1),
                RecurringType.NONE,
                "Yoga for beginners",
                5
        );

        // When
        workoutService.edit(workout, workoutRequest);

        // Then
        verify(workoutRepository).save(workoutCaptor.capture());
        Workout updatedWorkout = workoutCaptor.getValue();

        assertEquals(5, updatedWorkout.getMaxParticipants());
        assertEquals(3, updatedWorkout.getAvailableSpots());
    }

    @Test
    void givenWorkoutId_whenChangeStatusDeleted_thenWorkoutStatusChanged() {
        // Given
        UUID workoutId = UUID.randomUUID();
        Workout workout = Workout.builder()
                .id(workoutId)
                .status(WorkoutStatus.UPCOMING)
                .build();

        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));

        // When
        workoutService.changeStatusDeleted(workoutId);

        // Then
        verify(workoutRepository).save(workoutCaptor.capture());
        Workout updatedWorkout = workoutCaptor.getValue();

        assertEquals(WorkoutStatus.DELETED, updatedWorkout.getStatus());
    }

    @Test
    void givenNoFilters_whenGetAllDisplayedWorkouts_thenReturnUpcomingWorkouts() {
        // Given
        LocalDate today = LocalDate.now();
        List<Workout> workouts = Arrays.asList(
                Workout.builder().id(UUID.randomUUID()).build(),
                Workout.builder().id(UUID.randomUUID()).build()
        );

        when(workoutRepository.findWorkoutsForDisplay(today)).thenReturn(workouts);

        // When
        List<Workout> result = workoutService.getAllDisplayedWorkouts(today, null, null, null, null);

        // Then
        assertEquals(2, result.size());
        verify(workoutRepository).findWorkoutsForDisplay(today);
        verify(workoutRepository, never()).findAll(Mockito.<Specification<Workout>>any());
    }

    @Test
    void givenFilters_whenGetAllDisplayedWorkouts_thenReturnFilteredWorkouts() {
        // Given
        LocalDate today = LocalDate.now();
        WorkoutType workoutType = WorkoutType.YOGA;
        UUID trainerId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(1);
        String timeRange = "MORNING";

        List<Workout> workouts = Arrays.asList(
                Workout.builder().id(UUID.randomUUID()).build(),
                Workout.builder().id(UUID.randomUUID()).build()
        );

        when(workoutRepository.findAll(Mockito.<Specification<Workout>>any())).thenReturn(workouts);

        // When
        List<Workout> result = workoutService.getAllDisplayedWorkouts(today, workoutType, trainerId, date, timeRange);

        // Then
        assertEquals(2, result.size());
        verify(workoutRepository, never()).findWorkoutsForDisplay(any(LocalDate.class));
        verify(workoutRepository).findAll(Mockito.<Specification<Workout>>any());
    }

    @Test
    void givenTrainer_whenGetAllWorkoutsByTrainer_thenReturnTrainersWorkouts() {
        // Given
        UUID trainerId = UUID.randomUUID();
        User trainer = User.builder()
                .id(trainerId)
                .email("trainer@example.com")
                .role(UserRole.TRAINER)
                .build();

        List<Workout> workouts = Arrays.asList(
                Workout.builder().id(UUID.randomUUID()).build(),
                Workout.builder().id(UUID.randomUUID()).build()
        );

        when(userService.getById(trainerId)).thenReturn(trainer);
        when(workoutRepository.findAllByTrainerOrderByStartTimeDesc(trainer)).thenReturn(workouts);

        // When
        List<Workout> result = workoutService.getAllWorkoutsByTrainer(trainerId);

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void whenGetAllWorkouts_thenReturnAllWorkouts() {
        // Given
        List<Workout> workouts = Arrays.asList(
                Workout.builder().id(UUID.randomUUID()).build(),
                Workout.builder().id(UUID.randomUUID()).build()
        );

        when(workoutRepository.findAll()).thenReturn(workouts);

        // When
        List<Workout> result = workoutService.getAllWorkouts();

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void givenTrainer_whenCalculateMonthlyAttendancePercentage_thenReturnPercentage() {
        // Given
        User trainer = User.builder()
                .id(UUID.randomUUID())
                .email("trainer@example.com")
                .role(UserRole.TRAINER)
                .build();

        User client1 = User.builder().id(UUID.randomUUID()).build();
        User client2 = User.builder().id(UUID.randomUUID()).build();
        List<User> clients1 = Arrays.asList(client1, client2);

        User client3 = User.builder().id(UUID.randomUUID()).build();
        List<User> clients2 = Collections.singletonList(client3);

        LocalDateTime now = LocalDateTime.now();
        Workout workout1 = Workout.builder()
                .startTime(now)
                .status(WorkoutStatus.COMPLETED)
                .maxParticipants(4)
                .clients(clients1)
                .build();

        Workout workout2 = Workout.builder()
                .startTime(now)
                .status(WorkoutStatus.COMPLETED)
                .maxParticipants(2)
                .clients(clients2)
                .build();

        List<Workout> completedWorkouts = Arrays.asList(workout1, workout2);

        when(workoutRepository.findAllByTrainerAndStatusOrderByStartTime(trainer, WorkoutStatus.COMPLETED))
                .thenReturn(completedWorkouts);

        // When
        double percentage = workoutService.calculateMonthlyAttendancePercentage(trainer);

        // Then
        assertEquals(50.0, percentage);
    }

    @Test
    void givenNoCompletedWorkouts_whenCalculateMonthlyAttendancePercentage_thenReturnZero() {
        // Given
        User trainer = User.builder()
                .id(UUID.randomUUID())
                .email("trainer@example.com")
                .role(UserRole.TRAINER)
                .build();

        when(workoutRepository.findAllByTrainerAndStatusOrderByStartTime(trainer, WorkoutStatus.COMPLETED))
                .thenReturn(Collections.emptyList());

        // When
        double percentage = workoutService.calculateMonthlyAttendancePercentage(trainer);

        // Then
        assertEquals(0.0, percentage);
    }

    @Test
    void givenRecurringCompletedWorkouts_whenGetAllCompletedRecurringWorkouts_thenReturnWorkouts() {
        // Given
        List<Workout> recurringWorkouts = Arrays.asList(
                Workout.builder().id(UUID.randomUUID()).build(),
                Workout.builder().id(UUID.randomUUID()).build()
        );

        when(workoutRepository.findAllByStatusAndRecurringTypeNotAndNextRecurringCreatedFalse(
                WorkoutStatus.COMPLETED, RecurringType.NONE)).thenReturn(recurringWorkouts);

        // When
        List<Workout> result = workoutService.getAllCompletedRecurringWorkouts();

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void givenNewRecurringWorkout_whenSaveRecurringWorkouts_thenWorkoutsSaved() {
        // Given
        UUID completedWorkoutId = UUID.randomUUID();
        Workout completedWorkout = Workout.builder()
                .id(completedWorkoutId)
                .nextRecurringCreated(false)
                .build();

        Workout newWorkout = Workout.builder()
                .completedCloneWorkoutId(completedWorkoutId)
                .build();

        when(workoutRepository.findById(completedWorkoutId)).thenReturn(Optional.of(completedWorkout));

        // When
        workoutService.saveRecurringWorkouts(newWorkout);

        // Then
        verify(workoutRepository).save(newWorkout);
        verify(workoutRepository).save(completedWorkout);
        assertTrue(completedWorkout.isNextRecurringCreated());
    }

    @Test
    void givenClientId_whenGetAllCompletedMonthWorkoutsForClient_thenReturnCount() {
        // Given
        UUID clientId = UUID.randomUUID();
        User client = User.builder()
                .id(clientId)
                .email("client@example.com")
                .role(UserRole.CLIENT)
                .build();

        LocalDateTime now = LocalDateTime.now();
        Workout completedWorkout1 = Workout.builder()
                .id(UUID.randomUUID())
                .status(WorkoutStatus.COMPLETED)
                .startTime(now)
                .build();
        Workout completedWorkout2 = Workout.builder()
                .id(UUID.randomUUID())
                .status(WorkoutStatus.COMPLETED)
                .startTime(now.minusDays(2))
                .build();
        Workout upcomingWorkout = Workout.builder()
                .id(UUID.randomUUID())
                .status(WorkoutStatus.UPCOMING)
                .startTime(now.plusDays(1))
                .build();

        List<Workout> workouts = Arrays.asList(completedWorkout1, completedWorkout2, upcomingWorkout);

       when(userService.getById(clientId)).thenReturn(client);
       when(workoutService.getAllRegisteredClientWorkouts(clientId)).thenReturn(workouts);

        // When
        int count = workoutService.getAllCompletedMonthWorkoutsForClient(client);

        // Then
        assertEquals(2, count);
    }

    @Test
    void whenGetAllCompletedMonthWorkouts_thenReturnCount() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Workout completedWorkout1 = Workout.builder()
                .id(UUID.randomUUID())
                .status(WorkoutStatus.COMPLETED)
                .startTime(now)
                .build();
        Workout completedWorkout2 = Workout.builder()
                .id(UUID.randomUUID())
                .status(WorkoutStatus.COMPLETED)
                .startTime(now.minusDays(2))
                .build();

        List<Workout> workouts = Arrays.asList(completedWorkout1, completedWorkout2);

        when(workoutRepository.findAll()).thenReturn(workouts);

        // When
        int count = workoutService.getAllCompletedMonthWorkouts();

        // Then
        assertEquals(2, count);
    }

    @Test
    void givenTrainer_whenGetUpcomingWorkoutsByTrainer_thenReturnWorkouts() {
        // Given
        User trainer = User.builder()
                .id(UUID.randomUUID())
                .email("trainer@example.com")
                .role(UserRole.TRAINER)
                .build();

        List<Workout> workouts = Arrays.asList(
                Workout.builder().id(UUID.randomUUID()).build(),
                Workout.builder().id(UUID.randomUUID()).build()
        );

        when(workoutRepository.findAllByTrainerAndStatusOrderByStartTime(trainer, WorkoutStatus.UPCOMING))
                .thenReturn(workouts);

        // When
        List<Workout> result = workoutService.getUpcomingWorkoutsByTrainer(trainer);

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void givenTrainer_whenGetMonthCompletedWorkoutsByTrainer_thenReturnWorkouts() {
        // Given
        User trainer = User.builder()
                .id(UUID.randomUUID())
                .email("trainer@example.com")
                .role(UserRole.TRAINER)
                .build();

        LocalDateTime now = LocalDateTime.now();
        Workout currentMonthWorkout = Workout.builder()
                .id(UUID.randomUUID())
                .startTime(now)
                .build();
        Workout previousMonthWorkout = Workout.builder()
                .id(UUID.randomUUID())
                .startTime(now.minusMonths(1))
                .build();

        List<Workout> allCompletedWorkouts = Arrays.asList(currentMonthWorkout, previousMonthWorkout);

        when(workoutRepository.findAllByTrainerAndStatusOrderByStartTime(trainer, WorkoutStatus.COMPLETED))
                .thenReturn(allCompletedWorkouts);

        // When
        List<Workout> result = workoutService.getMonthCompletedWorkoutsByTrainer(trainer);

        // Then
        assertEquals(1, result.size());
        assertEquals(currentMonthWorkout, result.getFirst());
    }

    @Test
    void givenWorkout_whenSaveCompletedWorkouts_thenWorkoutSaved() {
        // Given
        Workout workout = Workout.builder()
                .id(UUID.randomUUID())
                .status(WorkoutStatus.COMPLETED)
                .build();

        // When
        workoutService.saveCompletedWorkouts(workout);

        // Then
        verify(workoutRepository).save(workout);
    }

    @Test
    void givenWorkout_whenSaveDefaultTrainerWorkout_thenWorkoutSaved() {
        // Given
        Workout workout = Workout.builder()
                .id(UUID.randomUUID())
                .build();

        // When
        workoutService.saveDefaultTrainerWorkout(workout);

        // Then
        verify(workoutRepository).save(workout);
    }
}
