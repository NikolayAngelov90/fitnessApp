package com.fitnessapp.workout.event;

import com.fitnessapp.workout.model.WorkoutType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
public class UpsertWorkoutEvent {

    WorkoutType workoutType;

    LocalDate startTime;

    int duration;

    int participants;

    private UUID trainerId;
}
