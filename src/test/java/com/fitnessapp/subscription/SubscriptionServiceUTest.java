package com.fitnessapp.subscription;

import com.fitnessapp.exception.SubscriptionNotFoundException;
import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.subscription.model.SubscriptionType;
import com.fitnessapp.subscription.repository.SubscriptionRepository;
import com.fitnessapp.subscription.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceUTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void whenGetAllGym_thenReturnAllGymSubscriptions() {
        // Given
        List<Subscription> expectedSubscriptions = Arrays.asList(
                Subscription.builder().type(SubscriptionType.GYM).build(),
                Subscription.builder().type(SubscriptionType.GYM).build()
        );
        when(subscriptionRepository.findAllByTypeOrderByPriceAsc(SubscriptionType.GYM))
                .thenReturn(expectedSubscriptions);

        // When
        List<Subscription> result = subscriptionService.getAllGym();

        // Then
        assertEquals(expectedSubscriptions.size(), result.size());
        assertEquals(expectedSubscriptions, result);
        verify(subscriptionRepository, times(1)).findAllByTypeOrderByPriceAsc(SubscriptionType.GYM);
    }

    @Test
    void whenGetAllGymPlusTrainer_thenReturnAllGymPlusTrainerSubscriptions() {
        // Given
        List<Subscription> expectedSubscriptions = Arrays.asList(
                Subscription.builder().type(SubscriptionType.GYM_PLUS_TRAINER).build(),
                Subscription.builder().type(SubscriptionType.GYM_PLUS_TRAINER).build()
        );
        when(subscriptionRepository.findAllByTypeOrderByPriceAsc(SubscriptionType.GYM_PLUS_TRAINER))
                .thenReturn(expectedSubscriptions);

        // When
        List<Subscription> result = subscriptionService.getAllGymPlusTrainer();

        // Then
        assertEquals(expectedSubscriptions.size(), result.size());
        assertEquals(expectedSubscriptions, result);
        verify(subscriptionRepository, times(1)).findAllByTypeOrderByPriceAsc(SubscriptionType.GYM_PLUS_TRAINER);
    }

    @Test
    void whenGetAll_thenReturnAllSubscriptionsOrderedByPrice() {
        // Given
        List<Subscription> expectedSubscriptions = Arrays.asList(
                Subscription.builder().price(new BigDecimal("50.00")).build(),
                Subscription.builder().price(new BigDecimal("100.00")).build()
        );
        when(subscriptionRepository.findAllByOrderByPriceAsc()).thenReturn(expectedSubscriptions);

        // When
        List<Subscription> result = subscriptionService.getAll();

        // Then
        assertEquals(expectedSubscriptions.size(), result.size());
        assertEquals(expectedSubscriptions, result);
        verify(subscriptionRepository, times(1)).findAllByOrderByPriceAsc();
    }

    @Test
    void givenExistingSubscriptionId_whenGetById_thenReturnSubscription() {
        // Given
        UUID subscriptionId = UUID.randomUUID();
        Subscription expectedSubscription = Subscription.builder()
                .id(subscriptionId)
                .build();
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(expectedSubscription));

        // When
        Subscription result = subscriptionService.getById(subscriptionId);

        // Then
        assertEquals(expectedSubscription, result);
        verify(subscriptionRepository, times(1)).findById(subscriptionId);
    }

    @Test
    void givenNonExistingSubscriptionId_whenGetById_thenThrowException() {
        // Given
        UUID subscriptionId = UUID.randomUUID();
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(SubscriptionNotFoundException.class, () -> subscriptionService.getById(subscriptionId));
        verify(subscriptionRepository, times(1)).findById(subscriptionId);
    }

    @Test
    void givenExistingSubscriptionIdAndNewPrice_whenUpdatePrice_thenUpdateSubscriptionPrice() {
        // Given
        UUID subscriptionId = UUID.randomUUID();
        BigDecimal oldPrice = new BigDecimal("50.00");
        BigDecimal newPrice = new BigDecimal("75.00");

        Subscription existingSubscription = Subscription.builder()
                .id(subscriptionId)
                .price(oldPrice)
                .build();

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(existingSubscription));

        // When
        subscriptionService.updatePrice(subscriptionId, newPrice);

        // Then
        assertEquals(subscriptionId, existingSubscription.getId());
        assertEquals(newPrice, existingSubscription.getPrice());
        verify(subscriptionRepository, times(1)).save(existingSubscription);
    }

    @Test
    void givenNonExistingSubscriptionId_whenUpdatePrice_thenThrowException() {
        // Given
        UUID subscriptionId = UUID.randomUUID();
        BigDecimal newPrice = new BigDecimal("75.00");

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(SubscriptionNotFoundException.class,
                () -> subscriptionService.updatePrice(subscriptionId, newPrice));

        verify(subscriptionRepository, never()).save(any());
    }

}
