package com.fitnessapp.web;

import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.membership.service.MembershipPlanService;
import com.fitnessapp.subscription.service.SubscriptionService;
import com.fitnessapp.payment.service.PaymentService;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.web.dto.PaymentRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/memberships")
public class MembershipController {

    private final MembershipPlanService membershipPlanService;
    private final SubscriptionService subscriptionService;
    private final PaymentService paymentService;
    private final UserService userService;


    public MembershipController(MembershipPlanService membershipPlanService,
                                SubscriptionService subscriptionService,
                                PaymentService paymentService,
                                UserService userService) {
        this.membershipPlanService = membershipPlanService;
        this.subscriptionService = subscriptionService;
        this.paymentService = paymentService;
        this.userService = userService;
    }


    @GetMapping("/{id}/payment")
    public ModelAndView showSubscriptionPaymentsPage(@PathVariable UUID id) {

        Subscription subscription = subscriptionService.getById(id);

        ModelAndView modelAndView = new ModelAndView("subscription-payment");
        modelAndView.addObject("subscription", subscription);
        modelAndView.addObject("paymentRequest", PaymentRequest.empty());

        return modelAndView;
    }

    @PostMapping("/{id}/payment")
    public ModelAndView processPayment(@Valid PaymentRequest paymentRequest,
                                       BindingResult bindingResult,
                                       @PathVariable UUID id,
                                       @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Subscription subscription = subscriptionService.getById(id);

        ModelAndView modelAndView = new ModelAndView("subscription-payment");
        modelAndView.addObject("subscription", subscription);

        if (bindingResult.hasErrors()) {
            return modelAndView;
        }

        User user = userService.getById(customUserDetails.getUserId());
        paymentService.processMembershipPayment(subscription, user, paymentRequest);

        return modelAndView;
    }

}
