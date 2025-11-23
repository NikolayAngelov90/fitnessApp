package com.fitnessapp.web;

import com.fitnessapp.membership.service.MembershipPlanService;
import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.subscription.service.SubscriptionService;
import com.fitnessapp.payment.service.PaymentService;
import com.fitnessapp.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MembershipControllerUTest {

    private MembershipPlanService membershipPlanService;
    private SubscriptionService subscriptionService;
    private PaymentService paymentService;
    private MembershipController controller;

    @BeforeEach
    void setUp() {
        membershipPlanService = mock(MembershipPlanService.class);
        subscriptionService = mock(SubscriptionService.class);
        paymentService = mock(PaymentService.class);
        controller = new MembershipController(membershipPlanService, subscriptionService, paymentService);
    }

    @Test
    void showMembershipPaymentsPage_returnsViewWithSubscription() {
        UUID id = UUID.randomUUID();
        Subscription sub = Subscription.builder().id(id).build();
        when(subscriptionService.getById(id)).thenReturn(sub);

        ModelAndView mv = controller.showMembershipPaymentsPage(id);
        assertEquals("subscription-payment", mv.getViewName());
        assertEquals(sub, mv.getModel().get("subscription"));
    }

    @Test
    void processPayment_processesAndReturnsMessage() {
        UUID id = UUID.randomUUID();
        UUID uid = UUID.randomUUID();
        Subscription sub = Subscription.builder().id(id).build();
        when(subscriptionService.getById(id)).thenReturn(sub);
        CustomUserDetails cud = new CustomUserDetails(uid, "e@e.com", "pwd", UserRole.CLIENT, false);

        ModelAndView mv = controller.processPayment(id, cud);
        assertEquals("subscription-payment", mv.getViewName());
        assertEquals(sub, mv.getModel().get("subscription"));
        assertEquals("Payment Successful", mv.getModel().get("message"));
        verify(paymentService).processMembershipPayment(sub, uid);
    }

    @Test
    void switchMembershipStatus_switchesAndRedirects() {
        UUID id = UUID.randomUUID();
        String view = controller.switchMembershipStatus(id);
        assertEquals("redirect:/home-client", view);
        verify(membershipPlanService).changeStatus(id);
    }
}
