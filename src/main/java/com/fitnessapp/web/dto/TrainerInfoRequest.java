package com.fitnessapp.web.dto;

import com.fitnessapp.utils.Validation.ValidPhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TrainerInfoRequest(
        @Size(max = 20, message = "Name must be a max 20 characters")
        @NotBlank(message = "Please insert first name")
        String firstName,

        @Size(max = 20, message = "Name must be a max 20 characters")
        @NotBlank(message = "Please insert last name")
        String lastName,

        @ValidPhoneNumber
        @NotBlank(message = "Please insert phone number")
        String phoneNumber,

        @Size(min = 3, max = 10, message = "Text must be between 3 and 10 symbols")
        @NotBlank(message = "Please insert specialization")
        String specialization,

        @Size(min = 10, max = 255, message = "Description must be between 10 and 255 symbols")
        @NotBlank(message = "Please insert description")
        String description
) {
    public static TrainerInfoRequest empty() {
        return new TrainerInfoRequest("", "", "", "", "");
    }
}
