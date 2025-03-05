package com.fitnessapp.exception;

import java.util.UUID;

public class ClientAlreadyHaveAnActiveMembershipException extends PaymentFailedException {
    public ClientAlreadyHaveAnActiveMembershipException(String message, UUID subscriptionId) {
        super(message, subscriptionId);
    }
}
