package com.fitnessapp.web.dto;

import com.fitnessapp.utils.Validation.ValidPhoneNumber;
import jakarta.validation.constraints.Size;

public record UserEditRequest(
        @Size(max = 20, message = "Name must be a max 20 characters") String firstName,
        @Size(max = 20, message = "Name must be a max 20 characters") String lastName,
        @ValidPhoneNumber String phoneNumber
) {
    public static UserEditRequest empty() {
        return new UserEditRequest("", "", "");
    }
}
