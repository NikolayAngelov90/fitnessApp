package com.fitnessapp.scheduler;

import com.fitnessapp.user.model.User;
import com.fitnessapp.workout.event.UpsertWorkoutEvent;
import com.fitnessapp.workout.model.RecurringType;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.model.WorkoutStatus;
import com.fitnessapp.workout.model.WorkoutType;
import com.fitnessapp.workout.property.WorkoutProperty;
import com.fitnessapp.workout.service.WorkoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkoutSchedulerUTest {

    @Mock
    private WorkoutService workoutService;
    @Mock
    private WorkoutProperty workoutProperty;
    @Mock
    private KafkaTemplate<String, UpsertWorkoutEvent> kafkaTemplate;
    @Captor
    private ArgumentCaptor<Workout> workoutCaptor;
    @Captor
    private ArgumentCaptor<UpsertWorkoutEvent> eventCaptor;
    @Captor
    private ArgumentCaptor<String> topicCaptor;

    @InjectMocks
    private WorkoutScheduler workoutScheduler;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
    }

    @Test
    void givenNoCompletedWorkouts_whenProcessWorkouts_thenNoCompletedWorkoutsUpdated() {
        // Given
        LocalDateTime futureTime = now.plusHours(1);
        Workout workout = Workout.builder()
                .id(UUID.randomUUID())
                .startTime(now)
                .endTime(futureTime)
                .status(WorkoutStatus.UPCOMING)
                .build();

        when(workoutService.getAllWorkouts()).thenReturn(List.of(workout));

        // When
        workoutScheduler.processWorkouts();

        // Then
        verify(workoutService, never()).saveCompletedWorkouts(any());
        verify(kafkaTemplate, never()).send(anyString(), any(UpsertWorkoutEvent.class));
    }

    @Test
    void givenAlreadyCompletedOrDeletedWorkouts_whenProcessWorkouts_thenNoStatusUpdate() {
        // Given
        LocalDateTime pastTime = now.minusHours(1);
        Workout completedWorkout = Workout.builder()
                .id(UUID.randomUUID())
                .startTime(pastTime.minusHours(2))
                .endTime(pastTime)
                .status(WorkoutStatus.COMPLETED)
                .build();
        Workout deletedWorkout = Workout.builder()
                .id(UUID.randomUUID())
                .startTime(pastTime.minusHours(2))
                .endTime(pastTime)
                .status(WorkoutStatus.DELETED)
                .build();

        when(workoutService.getAllWorkouts()).thenReturn(List.of(completedWorkout, deletedWorkout));

        // When
        workoutScheduler.processWorkouts();

        // Then
        verify(workoutService, never()).saveCompletedWorkouts(any());
        verify(kafkaTemplate, never()).send(anyString(), any(UpsertWorkoutEvent.class));
    }

    @Test
    void givenWorkoutsEndTimeInPast_whenProcessWorkouts_thenWorkoutsMarkedCompletedAndEventsSent() {
        // Given
        LocalDateTime pastTime = now.minusHours(1);
        User trainer = User.builder()
                .id(UUID.randomUUID())
                .build();

        Workout workout = Workout.builder()
                .id(UUID.randomUUID())
                .workoutType(WorkoutType.YOGA)
                .startTime(pastTime.minusMinutes(60))
                .endTime(pastTime)
                .status(WorkoutStatus.UPCOMING)
                .maxParticipants(10)
                .availableSpots(3)
                .duration(60)
                .trainer(trainer)
                .build();

        when(workoutService.getAllWorkouts()).thenReturn(List.of(workout));

        // When
        workoutScheduler.processWorkouts();

        // Then
        verify(workoutService, times(1)).saveCompletedWorkouts(workoutCaptor.capture());
        Workout capturedWorkout = workoutCaptor.getValue();
        assertEquals(WorkoutStatus.COMPLETED, capturedWorkout.getStatus());
        assertEquals(workout.getId(), capturedWorkout.getId());

        verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), eventCaptor.capture());
        String capturedTopic = topicCaptor.getValue();
        UpsertWorkoutEvent capturedEvent = eventCaptor.getValue();
        assertEquals("workout-events", capturedTopic);
        assertEquals(workout.getWorkoutType(), capturedEvent.getWorkoutType());
        assertEquals(workout.getStartTime().toLocalDate(), capturedEvent.getStartTime());
        assertEquals(60, capturedEvent.getDuration());
        assertEquals(7, capturedEvent.getParticipants());
        assertEquals(workout.getTrainer().getId(), capturedEvent.getTrainerId());
    }

    @Test
    void givenMultipleCompletedWorkouts_whenProcessWorkouts_thenAllUpdatedAndEventsSent() {
        // Given
        LocalDateTime pastTime = now.minusHours(1);
        User trainer1 = User.builder()
                .id(UUID.randomUUID())
                .build();
        User trainer2 = User.builder()
                .id(UUID.randomUUID())
                .build();

        Workout workout1 = Workout.builder()
                .id(UUID.randomUUID())
                .workoutType(WorkoutType.YOGA)
                .startTime(pastTime.minusMinutes(60))
                .endTime(pastTime)
                .status(WorkoutStatus.UPCOMING)
                .maxParticipants(10)
                .availableSpots(3)
                .duration(60)
                .trainer(trainer1)
                .build();
        Workout workout2 = Workout.builder()
                .id(UUID.randomUUID())
                .workoutType(WorkoutType.PILATES)
                .startTime(pastTime.minusMinutes(45))
                .endTime(pastTime)
                .status(WorkoutStatus.UPCOMING)
                .maxParticipants(8)
                .availableSpots(2)
                .duration(45)
                .trainer(trainer2)
                .build();

        when(workoutService.getAllWorkouts()).thenReturn(List.of(workout1, workout2));

        // When
        workoutScheduler.processWorkouts();

        // Then
        verify(workoutService, times(2)).saveCompletedWorkouts(workoutCaptor.capture());
        List<Workout> capturedWorkouts = workoutCaptor.getAllValues();
        assertEquals(2, capturedWorkouts.size());
        assertTrue(capturedWorkouts.stream().allMatch(w -> w.getStatus() == WorkoutStatus.COMPLETED));
        assertTrue(capturedWorkouts.stream().anyMatch(w -> w.getId() == workout1.getId()));
        assertTrue(capturedWorkouts.stream().anyMatch(w -> w.getId() == workout2.getId()));

        verify(kafkaTemplate, times(2)).send(topicCaptor.capture(), eventCaptor.capture());
        List<String> capturedTopics = topicCaptor.getAllValues();
        List<UpsertWorkoutEvent> capturedEvents = eventCaptor.getAllValues();
        assertEquals(2, capturedEvents.size());
        assertTrue(capturedTopics.stream().allMatch(t -> t.equals("workout-events")));
        assertTrue(capturedEvents.stream().anyMatch(e -> e.getWorkoutType() == WorkoutType.PILATES));
        assertTrue(capturedEvents.stream().anyMatch(e -> e.getWorkoutType() == WorkoutType.YOGA));
    }

    @Test
    void givenNoRecurringWorkouts_whenProcessWorkouts_thenNoRecurringWorkoutsScheduled() {
        // Given
        when(workoutService.getAllCompletedRecurringWorkouts()).thenReturn(Collections.emptyList());

        // When
        workoutScheduler.processWorkouts();

        // Then
        verify(workoutService, never()).saveRecurringWorkouts(any());
    }

    @Test
    void givenDailyRecurringWorkout_whenProcessWorkouts_thenNewWorkoutScheduledForNextDay() {
        // Given
        LocalDateTime pastTime = now.minusHours(1);
        User trainer = User.builder()
                .id(UUID.randomUUID())
                .build();

        Workout completedRecurringWorkout = Workout.builder()
                .id(UUID.randomUUID())
                .workoutType(WorkoutType.YOGA)
                .duration(60)
                .price(BigDecimal.valueOf(25))
                .startTime(pastTime)
                .endTime(pastTime.plusMinutes(60))
                .recurringType(RecurringType.DAILY)
                .trainer(trainer)
                .description("Daily yoga class")
                .createdAt(pastTime.minusDays(1))
                .maxParticipants(10)
                .availableSpots(0)
                .status(WorkoutStatus.COMPLETED)
                .build();

        when(workoutService.getAllCompletedRecurringWorkouts()).thenReturn(List.of(completedRecurringWorkout));
        when(workoutProperty.getDefaultStatus()).thenReturn(WorkoutStatus.UPCOMING);

        // When
        workoutScheduler.processWorkouts();

        // Then
        verify(workoutService, times(1)).saveRecurringWorkouts(workoutCaptor.capture());
        Workout capturedWorkout = workoutCaptor.getValue();

        assertEquals(completedRecurringWorkout.getWorkoutType(), capturedWorkout.getWorkoutType());
        assertEquals(60, capturedWorkout.getDuration());
        assertEquals(completedRecurringWorkout.getPrice(), capturedWorkout.getPrice());
        assertEquals(pastTime.plusDays(1), capturedWorkout.getStartTime());
        assertEquals(pastTime.plusDays(1).plusMinutes(60), capturedWorkout.getEndTime());
        assertEquals(RecurringType.DAILY, capturedWorkout.getRecurringType());
        assertEquals(trainer, capturedWorkout.getTrainer());
        assertEquals("Daily yoga class", capturedWorkout.getDescription());
        assertEquals(completedRecurringWorkout.getCreatedAt(), capturedWorkout.getCreatedAt());
        assertEquals(10, capturedWorkout.getMaxParticipants());
        assertEquals(10, capturedWorkout.getAvailableSpots());
        assertEquals(WorkoutStatus.UPCOMING, capturedWorkout.getStatus());
        assertEquals(completedRecurringWorkout.getId(), capturedWorkout.getCompletedCloneWorkoutId());
    }

    @Test
    void givenWeeklyRecurringWorkout_whenProcessWorkouts_thenNewWorkoutScheduledForNextWeek() {
        // Given
        LocalDateTime pastTime = now.minusHours(1);
        User trainer = User.builder()
                .id(UUID.randomUUID())
                .build();

        Workout completedRecurringWorkout = Workout.builder()
                .id(UUID.randomUUID())
                .workoutType(WorkoutType.PILATES)
                .duration(45)
                .price(BigDecimal.valueOf(30))
                .startTime(pastTime)
                .endTime(pastTime.plusMinutes(45))
                .recurringType(RecurringType.WEEKLY)
                .trainer(trainer)
                .description("Weekly pilates class")
                .createdAt(pastTime.minusDays(7))
                .maxParticipants(8)
                .availableSpots(2)
                .status(WorkoutStatus.COMPLETED)
                .build();

        when(workoutService.getAllCompletedRecurringWorkouts()).thenReturn(List.of(completedRecurringWorkout));
        when(workoutProperty.getDefaultStatus()).thenReturn(WorkoutStatus.UPCOMING);

        // When
        workoutScheduler.processWorkouts();

        // Then
        verify(workoutService, times(1)).saveRecurringWorkouts(workoutCaptor.capture());
        Workout capturedWorkout = workoutCaptor.getValue();

        assertEquals(completedRecurringWorkout.getWorkoutType(), capturedWorkout.getWorkoutType());
        assertEquals(pastTime.plusDays(7), capturedWorkout.getStartTime());
        assertEquals(pastTime.plusDays(7).plusMinutes(45), capturedWorkout.getEndTime());
        assertEquals(RecurringType.WEEKLY, capturedWorkout.getRecurringType());
        assertEquals(WorkoutStatus.UPCOMING, capturedWorkout.getStatus());
        assertEquals(8, capturedWorkout.getAvailableSpots());
    }

    @Test
    void givenMultipleRecurringWorkouts_whenProcessWorkouts_thenAllNewWorkoutsScheduled() {
        // Given
        LocalDateTime pastTime = now.minusHours(1);
        User trainer1 = User.builder()
                .id(UUID.randomUUID())
                .build();
        User trainer2 = User.builder()
                .id(UUID.randomUUID())
                .build();

        Workout dailyWorkout = Workout.builder()
                .id(UUID.randomUUID())
                .workoutType(WorkoutType.YOGA)
                .duration(60)
                .startTime(pastTime)
                .endTime(pastTime.plusMinutes(60))
                .recurringType(RecurringType.DAILY)
                .trainer(trainer1)
                .maxParticipants(10)
                .availableSpots(0)
                .status(WorkoutStatus.COMPLETED)
                .build();
        Workout weeklyWorkout = Workout.builder()
                .id(UUID.randomUUID())
                .workoutType(WorkoutType.PILATES)
                .duration(45)
                .startTime(pastTime)
                .endTime(pastTime.plusMinutes(45))
                .recurringType(RecurringType.WEEKLY)
                .trainer(trainer2)
                .maxParticipants(8)
                .availableSpots(2)
                .status(WorkoutStatus.COMPLETED)
                .build();

        when(workoutService.getAllCompletedRecurringWorkouts()).thenReturn(List.of(dailyWorkout, weeklyWorkout));
        when(workoutProperty.getDefaultStatus()).thenReturn(WorkoutStatus.UPCOMING);

        // When
        workoutScheduler.processWorkouts();

        // Then
        verify(workoutService, times(2)).saveRecurringWorkouts(workoutCaptor.capture());
        List<Workout> capturedWorkouts = workoutCaptor.getAllValues();

        assertEquals(2, capturedWorkouts.size());
        assertTrue(capturedWorkouts.stream().anyMatch(w -> w.getWorkoutType() == WorkoutType.YOGA &&
                w.getStartTime().equals(pastTime.plusDays(1))));
        assertTrue(capturedWorkouts.stream().anyMatch(w -> w.getWorkoutType() == WorkoutType.PILATES &&
                w.getStartTime().equals(pastTime.plusDays(7))));
    }

    @Test
    void givenInvalidRecurringType_whenProcessWorkouts_thenThrowsIllegalArgumentException() {
        // Given
        LocalDateTime pastTime = now.minusHours(1);
        Workout invalidRecurringWorkout = Workout.builder()
                .id(UUID.randomUUID())
                .workoutType(WorkoutType.YOGA)
                .startTime(pastTime)
                .endTime(pastTime.plusMinutes(60))
                .recurringType(null)
                .status(WorkoutStatus.COMPLETED)
                .build();

        when(workoutService.getAllCompletedRecurringWorkouts()).thenReturn(List.of(invalidRecurringWorkout));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> workoutScheduler.processWorkouts());
    }
}
