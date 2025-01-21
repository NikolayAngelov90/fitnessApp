package com.fitnessapp.membership.service;

import com.fitnessapp.membership.model.Subscription;
import com.fitnessapp.membership.model.SubscriptionDuration;
import com.fitnessapp.membership.model.SubscriptionType;
import com.fitnessapp.membership.repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
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
                            .type(SubscriptionType.GymOnly)
                            .price(new BigDecimal("70.00"))
                            .duration(SubscriptionDuration.ONE_MONTH)
                            .build(),
                    Subscription.builder()
                            .type(SubscriptionType.GymOnly)
                            .price(new BigDecimal("140.00"))
                            .duration(SubscriptionDuration.THREE_MONTHS)
                            .build(),
                    Subscription.builder()
                            .type(SubscriptionType.GymOnly)
                            .price(new BigDecimal("280.00"))
                            .duration(SubscriptionDuration.SIX_MONTHS)
                            .build(),
                    Subscription.builder()
                            .type(SubscriptionType.GymOnly)
                            .price(new BigDecimal("560.00"))
                            .duration(SubscriptionDuration.ONE_YEAR)
                            .build(),
                    Subscription.builder()
                            .type(SubscriptionType.GymPlusTrainer)
                            .price(new BigDecimal("150.00"))
                            .duration(SubscriptionDuration.ONE_MONTH)
                            .build(),
                    Subscription.builder()
                            .type(SubscriptionType.GymPlusTrainer)
                            .price(new BigDecimal("350.00"))
                            .duration(SubscriptionDuration.THREE_MONTHS)
                            .build());

            subscriptionRepository.saveAll(subscriptions);
            log.info("Subscriptions seeded in the database.");
        }
    }
}
