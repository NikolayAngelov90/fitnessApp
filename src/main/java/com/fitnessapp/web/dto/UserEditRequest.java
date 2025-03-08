package com.fitnessapp.web.dto;

import com.fitnessapp.utils.Validation.ValidPhoneNumber;
import jakarta.validation.constraints.Size;

public record UserEditRequest(

        @Size(max = 25, message = "Name must be a max 20 characters")
        String firstName,

        @Size(max = 25, message = "Name must be a max 20 characters")
        String lastName,

        @ValidPhoneNumber
        String phoneNumber,

        @Size(min = 3, max = 30, message = "Text must be between 3 and 30 symbols")
        String specialization,

        @Size(min = 10, max = 255, message = "Description must be between 10 and 255 symbols")
        String description
) {
}
