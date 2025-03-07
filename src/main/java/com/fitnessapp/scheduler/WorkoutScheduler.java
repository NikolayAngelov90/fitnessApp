package com.fitnessapp.scheduler;

import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.model.WorkoutStatus;
import com.fitnessapp.workout.service.WorkoutService;
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
                .filter(w -> w.getEndTime().isAfter(now))
                .toList();

        if (completedWorkouts.isEmpty()) {
            log.info("No workouts is completed");
        }

        completedWorkouts.forEach(w -> {
            w.setStatus(WorkoutStatus.COMPLETED);
            workoutService.saveChangeStatusWorkouts(w);

            log.info("Workout [{}] has been completed", w.getId());
        });
    }

    @Scheduled(cron = "0 59 23 * * ?")
    public void scheduleRecurringWorkouts() {

        List<Workout> recurringWorkouts = workoutService.getAllCompletedRecurringWorkouts();

        if (recurringWorkouts.isEmpty()) {
            log.info("No workouts for recurring");
        }

        for (Workout recurringWorkout : recurringWorkouts) {

            LocalDateTime newDate = calculateNextDate(recurringWorkout);
            recurringWorkout.setStartTime(newDate);
            recurringWorkout.setEndTime(newDate.plusMinutes(recurringWorkout.getDuration()));
            recurringWorkout.setStatus(WorkoutStatus.UPCOMING);

            workoutService.saveChangeStatusWorkouts(recurringWorkout);
        }
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
