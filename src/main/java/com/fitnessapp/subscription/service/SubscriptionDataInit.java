package com.fitnessapp.subscription.service;

import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.subscription.model.SubscriptionDuration;
import com.fitnessapp.subscription.model.SubscriptionType;
import com.fitnessapp.subscription.repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class SubscriptionDataInit implements CommandLineRunner {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionDataInit(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public void run(String... args) {

        if (subscriptionRepository.count() == 0) {
            List<Subscription> subscriptions = List.of(
                    Subscription.builder()
                            .type(SubscriptionType.GYM)
                            .price(new BigDecimal("70.00"))
                            .duration(SubscriptionDuration.ONE_MONTH)
                            .build(),
                    Subscription.builder()
                            .type(SubscriptionType.GYM)
                            .price(new BigDecimal("140.00"))
                            .duration(SubscriptionDuration.THREE_MONTHS)
                            .build(),
                    Subscription.builder()
                            .type(SubscriptionType.GYM)
                            .price(new BigDecimal("280.00"))
                            .duration(SubscriptionDuration.SIX_MONTHS)
                            .build(),
                    Subscription.builder()
                            .type(SubscriptionType.GYM)
                            .price(new BigDecimal("560.00"))
                            .duration(SubscriptionDuration.ONE_YEAR)
                            .build(),
                    Subscription.builder()
                            .type(SubscriptionType.GYM_PLUS_TRAINER)
                            .price(new BigDecimal("150.00"))
                            .duration(SubscriptionDuration.ONE_MONTH)
                            .build(),
                    Subscription.builder()
                            .type(SubscriptionType.GYM_PLUS_TRAINER)
                            .price(new BigDecimal("350.00"))
                            .duration(SubscriptionDuration.THREE_MONTHS)
                            .build());

            subscriptionRepository.saveAll(subscriptions);
            log.info("Subscriptions seeded in the database.");
        }
    }
}
