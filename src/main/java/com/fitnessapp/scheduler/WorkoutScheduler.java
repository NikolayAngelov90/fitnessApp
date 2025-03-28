package com.fitnessapp.scheduler;

import com.fitnessapp.workout.event.UpsertWorkoutEvent;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.model.WorkoutStatus;
import com.fitnessapp.workout.property.WorkoutProperty;
import com.fitnessapp.workout.service.WorkoutService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class WorkoutScheduler {

    private final WorkoutService workoutService;
    private final WorkoutProperty workoutProperty;
    private final KafkaTemplate<String, UpsertWorkoutEvent> kafkaTemplate;

    public WorkoutScheduler(WorkoutService workoutService,
                            WorkoutProperty workoutProperty,
                            KafkaTemplate<String, UpsertWorkoutEvent> kafkaTemplate) {
        this.workoutService = workoutService;
        this.workoutProperty = workoutProperty;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void processWorkouts() {

        updateCompletedWorkouts();

        scheduleRecurringWorkouts();
    }

    private void updateCompletedWorkouts() {
        LocalDateTime now = LocalDateTime.now();

        List<Workout> completedWorkouts = workoutService.getAllWorkouts()
                .stream()
                .filter(w -> w.getEndTime().isBefore(now))
                .filter(w -> w.getStatus() != WorkoutStatus.COMPLETED)
                .filter(w -> w.getStatus() != WorkoutStatus.DELETED)
                .toList();

        if (completedWorkouts.isEmpty()) {
            log.info("No workouts is completed");
            return;
        }

        completedWorkouts.forEach(workout -> {
            workout.setStatus(WorkoutStatus.COMPLETED);
            workoutService.saveCompletedWorkouts(workout);

            log.info("Workout [{}] has been completed", workout.getId());

            UpsertWorkoutEvent event = UpsertWorkoutEvent.builder()
                    .workoutType(workout.getWorkoutType())
                    .startTime(workout.getStartTime().toLocalDate())
                    .duration(workout.getDuration())
                    .participants(workout.getMaxParticipants() - workout.getAvailableSpots())
                    .trainerId(workout.getTrainer().getId())
                    .build();

            log.info("Start - Sending WorkoutEvent {} to Kafka topic workout-events", event);
            kafkaTemplate.send("workout-events", event);
            log.info("End - Sending WorkoutEvent {} to Kafka topic workout-events", event);
        });
    }

    private void scheduleRecurringWorkouts() {
        List<Workout> recurringWorkouts = workoutService.getAllCompletedRecurringWorkouts();

        if (recurringWorkouts.isEmpty()) {
            log.info("No workouts for recurring");
            return;
        }

        recurringWorkouts.forEach(workout -> {
            Workout newWorkout = cloneWorkoutWithNewDate(workout);
            workoutService.saveRecurringWorkouts(newWorkout);

            log.info("Workout [{}] has been recurring", workout.getId());
        });
    }

    private Workout cloneWorkoutWithNewDate(Workout recurringWorkout) {
        LocalDateTime newDate = calculateNextDate(recurringWorkout);

        return Workout.builder()
                .workoutType(recurringWorkout.getWorkoutType())
                .duration(recurringWorkout.getDuration())
                .price(recurringWorkout.getPrice())
                .startTime(newDate)
                .endTime(newDate.plusMinutes(recurringWorkout.getDuration()))
                .recurringType(recurringWorkout.getRecurringType())
                .trainer(recurringWorkout.getTrainer())
                .description(recurringWorkout.getDescription())
                .createdAt(recurringWorkout.getCreatedAt())
                .maxParticipants(recurringWorkout.getMaxParticipants())
                .availableSpots(recurringWorkout.getMaxParticipants())
                .status(workoutProperty.getDefaultStatus())
                .completedCloneWorkoutId(recurringWorkout.getId())
                .build();
    }

    private LocalDateTime calculateNextDate(Workout recurringWorkout) {
        switch (recurringWorkout.getRecurringType()) {
            case DAILY -> {
                return recurringWorkout.getStartTime().plusDays(1);
            }
            case WEEKLY -> {
                return recurringWorkout.getStartTime().plusDays(7);
            }
            case null, default -> throw new IllegalArgumentException("Invalid Recurring Workout Type");
        }
    }
}
