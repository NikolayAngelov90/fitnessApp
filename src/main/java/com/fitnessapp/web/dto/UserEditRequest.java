package com.fitnessapp.web.dto;

import jakarta.validation.constraints.Size;

public record UserEditRequest(
        @Size(max = 50) String firstName,
        @Size(max = 50) String lastName
) {
}
