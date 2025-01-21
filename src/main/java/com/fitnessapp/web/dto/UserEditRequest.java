package com.fitnessapp.web.dto;

import com.fitnessapp.user.model.User;
import jakarta.validation.constraints.Size;

public record UserEditRequest(
        @Size(max = 50) String firstName,
        @Size(max = 50) String lastName
) {
    public static UserEditRequest buildFromUser(User user) {
        return new UserEditRequest(
                user.getFirstName(),
                user.getLastName());
    }
}
