package com.fitnessapp.exception;

import com.fitnessapp.payment.model.PaymentProductType;


public class DuplicateRegistrationClientWorkout extends PaymentFailedException {
    public DuplicateRegistrationClientWorkout(String message) {
        super(message, PaymentProductType.WORKOUT);
    }
}
