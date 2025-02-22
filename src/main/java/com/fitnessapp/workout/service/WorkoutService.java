package com.fitnessapp.workout.service;

import com.fitnessapp.exception.WorkoutNotFoundException;
import com.fitnessapp.workout.model.Workout;
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
}
