package com.fitnessapp.scheduler;

import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.model.WorkoutStatus;
import com.fitnessapp.workout.service.WorkoutService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class WorkoutScheduler {

    private final WorkoutService workoutService;

    public WorkoutScheduler(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @Scheduled(fixedRate = 300000)
    public void updateCompletedWorkouts() {

        LocalDateTime now = LocalDateTime.now();

        List<Workout> completedWorkouts = workoutService.getAllWorkouts()
                .stream()
                .filter(w -> w.getEndTime().isBefore(now))
                .toList();

        if (completedWorkouts.isEmpty()) {
            log.info("No workouts is completed");
        }

        completedWorkouts.forEach(w -> {
            w.setStatus(WorkoutStatus.COMPLETED);
            workoutService.saveWorkouts(w);

            log.info("Workout [{}] has been completed", w.getId());
        });
    }

    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void scheduleRecurringWorkouts() {

        List<Workout> recurringWorkouts = workoutService.getAllCompletedRecurringWorkouts();

        if (recurringWorkouts.isEmpty()) {
            log.info("No workouts for recurring");
            return;
        }

        recurringWorkouts.forEach(w -> {
            Workout newWorkout = cloneWorkoutWithNewDate(w);
            workoutService.saveWorkouts(newWorkout);
        });
    }

    private Workout cloneWorkoutWithNewDate(Workout originalWorkout) {
        LocalDateTime newDate = calculateNextDate(originalWorkout);

        return Workout.builder()
                .type(originalWorkout.getType())
                .duration(originalWorkout.getDuration())
                .price(originalWorkout.getPrice())
                .startTime(newDate)
                .endTime(newDate.plusMinutes(originalWorkout.getDuration()))
                .recurringType(originalWorkout.getRecurringType())
                .trainer(originalWorkout.getTrainer())
                .description(originalWorkout.getDescription())
                .createdAt(originalWorkout.getCreatedAt())
                .maxParticipants(originalWorkout.getMaxParticipants())
                .availableSpots(originalWorkout.getAvailableSpots())
                .status(WorkoutStatus.UPCOMING)
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
