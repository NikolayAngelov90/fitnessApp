package com.fitnessapp.exception;

public class WorkoutReportNotFoundException extends RuntimeException {
    public WorkoutReportNotFoundException(String message) {
        super(message);
    }
}
