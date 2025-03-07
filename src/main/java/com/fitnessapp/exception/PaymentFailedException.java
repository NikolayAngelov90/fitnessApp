package com.fitnessapp.exception;

import com.fitnessapp.payment.model.PaymentProductType;
import lombok.Getter;

import java.util.UUID;

@Getter
public class PaymentFailedException extends RuntimeException {

    private final UUID productId;
    private final PaymentProductType productType;

    public PaymentFailedException(String message, UUID productId, PaymentProductType productType) {
        super(message);
        this.productId = productId;
        this.productType = productType;
    }
}
