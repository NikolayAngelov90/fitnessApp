package com.fitnessapp.web.dto;

import com.fitnessapp.user.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email(message = "Invalid email format.")
        String email,

        @Size(min = 6, message = "Password must be at least 6 character.")
        String password,

        UserRole userRole
) {
        public static RegisterRequest empty() {
                return new RegisterRequest("", "",  null);
        }
}
