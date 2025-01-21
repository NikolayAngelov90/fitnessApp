package com.fitnessapp.workout.property;

import com.fitnessapp.workout.model.WorkoutStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "workouts")
public class WorkoutProperty {

    private WorkoutStatus defaultStatus;
}
