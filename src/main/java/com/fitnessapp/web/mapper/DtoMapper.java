package com.fitnessapp.web.mapper;

import com.fitnessapp.user.model.User;
import com.fitnessapp.web.dto.UserEditRequest;
import com.fitnessapp.web.dto.WorkoutRequest;
import com.fitnessapp.workout.model.Workout;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static UserEditRequest mapUserToUserEditRequest(User user) {

        return new UserEditRequest(
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getSpecialization(),
                user.getDescription());
    }

    public static WorkoutRequest mapWorkoutToWorkoutRequest(Workout workout) {

        return new WorkoutRequest(
                workout.getWorkoutType(),
                workout.getDuration(),
                workout.getPrice(),
                workout.getStartTime(),
                workout.getRecurringType(),
                workout.getDescription(),
                workout.getMaxParticipants());
    }
}
