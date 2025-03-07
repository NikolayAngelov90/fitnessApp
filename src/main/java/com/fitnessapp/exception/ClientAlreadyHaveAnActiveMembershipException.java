package com.fitnessapp.exception;

import com.fitnessapp.payment.model.PaymentProductType;

import java.util.UUID;

public class ClientAlreadyHaveAnActiveMembershipException extends PaymentFailedException {

    public ClientAlreadyHaveAnActiveMembershipException(String message, UUID productId) {
        super(message, productId, PaymentProductType.SUBSCRIPTION);
    }
}
