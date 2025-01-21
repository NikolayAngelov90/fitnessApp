package com.fitnessapp.workout.service;

import com.fitnessapp.workout.repository.WorkoutRepository;
import org.springframework.stereotype.Service;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;

    public WorkoutService(WorkoutRepository workoutRepository) {
        this.workoutRepository = workoutRepository;
    }


}
