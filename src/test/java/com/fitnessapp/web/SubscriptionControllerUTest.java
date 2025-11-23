package com.fitnessapp.web;

import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.subscription.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SubscriptionControllerUTest {

    private SubscriptionService subscriptionService;
    private SubscriptionController controller;

    @BeforeEach
    void setUp() {
        subscriptionService = mock(SubscriptionService.class);
        controller = new SubscriptionController(subscriptionService);
    }

    @Test
    void getSubscriptionsPage_returnsPlansWithGymAndGymPlusTrainer() {
        when(subscriptionService.getAllGym()).thenReturn(List.of(Subscription.builder().build()));
        when(subscriptionService.getAllGymPlusTrainer()).thenReturn(List.of(Subscription.builder().build()));

        ModelAndView mv = controller.getSubscriptionsPage();
        assertEquals("plans", mv.getViewName());
        assertNotNull(mv.getModel().get("gym"));
        assertNotNull(mv.getModel().get("gymPlusTrainer"));
    }

    @Test
    void updateSubscriptionPrice_callsService_andRedirects() {
        UUID id = UUID.randomUUID();
        String view = controller.updateSubscriptionPrice(id, BigDecimal.TEN);
        assertEquals("redirect:/home-admin", view);
        verify(subscriptionService).updatePrice(id, BigDecimal.TEN);
    }
}
