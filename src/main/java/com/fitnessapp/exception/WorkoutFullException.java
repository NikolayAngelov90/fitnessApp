package com.fitnessapp.exception;

public class WorkoutFullException extends RuntimeException {
    public WorkoutFullException(String message) {
        super(message);
    }
}
