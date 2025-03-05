package com.fitnessapp.workout.service;

import com.fitnessapp.exception.DuplicateRegistrationClientWorkout;
import com.fitnessapp.exception.WorkoutFullException;
import com.fitnessapp.exception.WorkoutNotFoundException;
import com.fitnessapp.user.model.User;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.model.WorkoutStatus;
import com.fitnessapp.workout.repository.WorkoutRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;

    public WorkoutService(WorkoutRepository workoutRepository) {
        this.workoutRepository = workoutRepository;
    }

    public void registerClient(Workout workout, User user) {

        validateRegistration(user, workout);

        workout.getClients().add(user);
        workout.setAvailableSpots(workout.getAvailableSpots() - 1);

        if (workout.getAvailableSpots() == 0) {
            workout.setStatus(WorkoutStatus.FULL);
        }

        workoutRepository.save(workout);
    }

    public List<Workout> getAllWorkouts() {

        List<Workout> workouts = workoutRepository.findAll();
        if (workouts.isEmpty()) {
            throw new WorkoutNotFoundException("Workout not found");
        }

        return workouts;
    }

    public Workout getById(UUID id) {
        return workoutRepository.findById(id).orElseThrow(() -> new WorkoutNotFoundException("Workout not found"));
    }

    private void validateRegistration(User user, Workout workout) {
        if (workout.getStatus() == WorkoutStatus.FULL) {
            throw new WorkoutFullException("Workout is already full");
        }

        if (workout.getClients().contains(user)) {
            throw new DuplicateRegistrationClientWorkout("You already book this workout");
        }
    }
}
