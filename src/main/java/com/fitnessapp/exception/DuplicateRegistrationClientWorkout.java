package com.fitnessapp.exception;

public class DuplicateRegistrationClientWorkout extends RuntimeException {
    public DuplicateRegistrationClientWorkout(String message) {
        super(message);
    }
}
