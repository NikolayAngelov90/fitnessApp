package com.fitnessapp.web.dto;

import com.fitnessapp.user.model.UserRole;
import jakarta.validation.constraints.NotNull;

public record SwitchUserRoleRequest(

        @NotNull(message = "Please choose a role") UserRole userRole
) {
    public static SwitchUserRoleRequest empty() {
        return new SwitchUserRoleRequest(null);
    }
}
