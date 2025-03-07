package com.fitnessapp.exception;

import com.fitnessapp.payment.model.PaymentProductType;

import java.util.UUID;

public class DuplicateRegistrationClientWorkout extends PaymentFailedException {
    public DuplicateRegistrationClientWorkout(String message, UUID workoutsId) {
        super(message, workoutsId, PaymentProductType.WORKOUT);
    }
}
