package com.fitnessapp.web.dto;

import com.fitnessapp.workout.model.RecurringType;
import com.fitnessapp.workout.model.WorkoutType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WorkoutRequest(

        @NotNull(message = "Please insert workout type")
        WorkoutType workoutType,

        @NotNull(message = "Please insert duration")
        @Min(20)
        Integer duration,

        @NotNull(message = "Please insert price")
        BigDecimal price,

        @NotNull(message = "Please insert time")
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        LocalDateTime startTime,

        @NotNull(message = "Please insert recurring type")
        RecurringType recurringType,

        @NotBlank(message = "Please insert description")
        @Size(min = 10, max = 255, message = "Description must be between 10 and 255 symbols")
        String description,

        @NotNull(message = "Please insert numbers of participants")
        @Min(5)
        Integer maxParticipants
) {
    public static WorkoutRequest empty() {
        return new WorkoutRequest(null, null, null, null, null, "", null);
    }
}
