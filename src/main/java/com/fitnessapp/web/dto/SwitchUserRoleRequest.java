package com.fitnessapp.web.dto;

import com.fitnessapp.user.model.UserRole;

public record SwitchUserRoleRequest(
        UserRole userRole
) {
    public static SwitchUserRoleRequest empty() {
        return new SwitchUserRoleRequest(null);
    }
}
