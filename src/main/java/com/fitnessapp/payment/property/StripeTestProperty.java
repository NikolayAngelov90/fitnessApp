package com.fitnessapp.payment.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "stripe.test")
public class StripeTestProperty {

    private String token;

}
