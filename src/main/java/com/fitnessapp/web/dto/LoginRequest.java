package com.fitnessapp.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Email(message = "Invalid email format")
        String email,

        @Size(min = 6, message = "Password must be at least 6 character")
        String password
) {
}
