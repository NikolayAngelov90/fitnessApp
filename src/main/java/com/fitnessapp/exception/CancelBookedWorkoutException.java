package com.fitnessapp.exception;

public class CancelBookedWorkoutException extends RuntimeException {
    public CancelBookedWorkoutException(String message) {
        super(message);
    }
}
