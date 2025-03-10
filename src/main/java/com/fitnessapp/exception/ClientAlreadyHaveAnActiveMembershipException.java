package com.fitnessapp.exception;

import com.fitnessapp.payment.model.PaymentProductType;

public class ClientAlreadyHaveAnActiveMembershipException extends PaymentFailedException {

    public ClientAlreadyHaveAnActiveMembershipException(String message) {
        super(message, PaymentProductType.SUBSCRIPTION);
    }
}
