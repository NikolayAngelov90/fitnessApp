package com.fitnessapp.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class PaymentFailedException extends RuntimeException {

    private final UUID subscriptionId;

    public PaymentFailedException(String message, UUID subscriptionId) {
        super(message);
        this.subscriptionId = subscriptionId;
    }
}
