package com.fitnessapp.exception;

import com.fitnessapp.payment.model.PaymentProductType;

public class WorkoutFullException extends PaymentFailedException {
    public WorkoutFullException(String message) {
        super(message, PaymentProductType.WORKOUT);
    }
}
