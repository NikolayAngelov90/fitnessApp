package com.fitnessapp.web;

import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.membership.service.MembershipPlanService;
import com.fitnessapp.subscription.service.SubscriptionService;
import com.fitnessapp.payment.service.PaymentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/memberships")
public class MembershipController {

    private final MembershipPlanService membershipPlanService;
    private final SubscriptionService subscriptionService;
    private final PaymentService paymentService;


    public MembershipController(MembershipPlanService membershipPlanService,
                                SubscriptionService subscriptionService,
                                PaymentService paymentService) {
        this.membershipPlanService = membershipPlanService;
        this.subscriptionService = subscriptionService;
        this.paymentService = paymentService;
    }


    @GetMapping("/{id}/payment")
    @PreAuthorize("hasRole('CLIENT')")
    public ModelAndView showMembershipPaymentsPage(@PathVariable UUID id) {

        Subscription subscription = subscriptionService.getById(id);

        ModelAndView modelAndView = new ModelAndView("subscription-payment");
        modelAndView.addObject("subscription", subscription);

        return modelAndView;
    }

    @PostMapping("/{id}/payment")
    public ModelAndView processPayment(@PathVariable UUID id,
                                       @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Subscription subscription = subscriptionService.getById(id);

        ModelAndView modelAndView = new ModelAndView("subscription-payment");
        modelAndView.addObject("subscription", subscription);

        paymentService.processMembershipPayment(subscription, customUserDetails.getUserId());

        modelAndView.addObject("message", "Payment Successful");

        return modelAndView;
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('CLIENT')")
    public String switchMembershipStatus(@PathVariable UUID id) {

        membershipPlanService.changeStatus(id);

        return "redirect:/home";
    }

}
