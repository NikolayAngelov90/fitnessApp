package com.fitnessapp.subscription;

import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.subscription.model.SubscriptionDuration;
import com.fitnessapp.subscription.model.SubscriptionType;
import com.fitnessapp.subscription.repository.SubscriptionRepository;
import com.fitnessapp.subscription.service.SubscriptionDataInit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionDataInitUTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Captor
    private ArgumentCaptor<List<Subscription>> subscriptionsCaptor;

    @InjectMocks
    private SubscriptionDataInit subscriptionDataInit;


    @Test
    void givenEmptyDatabase_whenRun_thenSeedSubscriptions() {
        // Given
        when(subscriptionRepository.count()).thenReturn(0L);

        // When
        subscriptionDataInit.run();

        // Then
        verify(subscriptionRepository).saveAll(subscriptionsCaptor.capture());
        List<Subscription> capturedSubscriptions = subscriptionsCaptor.getValue();

        assertEquals(6, capturedSubscriptions.size());

        // Verify GYM ONE_MONTH subscription
        Subscription gymOneMonth = findSubscription(capturedSubscriptions, SubscriptionType.GYM, SubscriptionDuration.ONE_MONTH);
        assertNotNull(gymOneMonth);
        assertEquals(new BigDecimal("70.00"), gymOneMonth.getPrice());

        // Verify GYM THREE_MONTHS subscription
        Subscription gymThreeMonths = findSubscription(capturedSubscriptions, SubscriptionType.GYM, SubscriptionDuration.THREE_MONTHS);
        assertNotNull(gymThreeMonths);
        assertEquals(new BigDecimal("140.00"), gymThreeMonths.getPrice());

        // Verify GYM SIX_MONTHS subscription
        Subscription gymSixMonths = findSubscription(capturedSubscriptions, SubscriptionType.GYM, SubscriptionDuration.SIX_MONTHS);
        assertNotNull(gymSixMonths);
        assertEquals(new BigDecimal("280.00"), gymSixMonths.getPrice());

        // Verify GYM ONE_YEAR subscription
        Subscription gymOneYear = findSubscription(capturedSubscriptions, SubscriptionType.GYM, SubscriptionDuration.ONE_YEAR);
        assertNotNull(gymOneYear);
        assertEquals(new BigDecimal("560.00"), gymOneYear.getPrice());

        // Verify GYM_PLUS_TRAINER ONE_MONTH subscription
        Subscription trainerOneMonth = findSubscription(capturedSubscriptions, SubscriptionType.GYM_PLUS_TRAINER, SubscriptionDuration.ONE_MONTH);
        assertNotNull(trainerOneMonth);
        assertEquals(new BigDecimal("150.00"), trainerOneMonth.getPrice());

        // Verify GYM_PLUS_TRAINER THREE_MONTHS subscription
        Subscription trainerThreeMonths = findSubscription(capturedSubscriptions, SubscriptionType.GYM_PLUS_TRAINER, SubscriptionDuration.THREE_MONTHS);
        assertNotNull(trainerThreeMonths);
        assertEquals(new BigDecimal("350.00"), trainerThreeMonths.getPrice());
    }

    @Test
    void givenNonEmptyDatabase_whenRun_thenDoNotSeedSubscriptions() {
        // Given
        when(subscriptionRepository.count()).thenReturn(6L);

        // When
        subscriptionDataInit.run();

        // Then
        verify(subscriptionRepository, never()).saveAll(any());
    }

    private Subscription findSubscription(List<Subscription> subscriptions, SubscriptionType type, SubscriptionDuration duration) {
        return subscriptions.stream()
                .filter(s -> s.getType() == type && s.getDuration() == duration)
                .findFirst()
                .orElse(null);
    }
}
