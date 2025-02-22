package com.fitnessapp.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.CreditCardNumber;

public record PaymentRequest(
        @NotBlank(message = "Enter valid card number") @CreditCardNumber(message = "Invalid card number")
        String cardNumber,
        @NotBlank(message = "Enter valid expiry") @Pattern(regexp = "\\d{2}/\\d{2}", message = "Invalid expiry")
        String expiry,
        @NotBlank(message = "Enter valid cvv") @Size(min = 3, max = 3, message = "Invalid cvv")
        String cvv
) {
    public static PaymentRequest empty() {
        return new PaymentRequest("", "", "");
    }
}
