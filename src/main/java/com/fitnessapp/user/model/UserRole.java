package com.fitnessapp.user.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum UserRole {

    CLIENT,
    TRAINER,
    ADMIN;

    public static List<UserRole> getRegistrableRoles() {

        return Stream.of(CLIENT, TRAINER)
                .collect(Collectors.toList());
    }
}
