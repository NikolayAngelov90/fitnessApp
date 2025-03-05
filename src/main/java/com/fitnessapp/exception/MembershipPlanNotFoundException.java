package com.fitnessapp.exception;

public class MembershipPlanNotFoundException extends RuntimeException {
    public MembershipPlanNotFoundException(String message) {
        super(message);
    }
}

