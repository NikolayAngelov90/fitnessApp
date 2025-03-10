package com.fitnessapp.exception;

import com.fitnessapp.payment.model.PaymentProductType;
import lombok.Getter;

@Getter
public class PaymentFailedException extends RuntimeException {

    private final PaymentProductType productType;

    public PaymentFailedException(String message, PaymentProductType productType) {
        super(message);
        this.productType = productType;
    }
}
