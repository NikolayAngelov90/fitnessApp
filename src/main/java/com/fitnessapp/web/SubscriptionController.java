package com.fitnessapp.web;

import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.subscription.service.SubscriptionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/plans")
public class SubscriptionController {

    private final SubscriptionService  subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public ModelAndView getSubscriptionsPage() {

        List<Subscription> allGym = subscriptionService.getAllGym();
        List<Subscription> allGymPlusTrainer = subscriptionService.getAllGymPlusTrainer();

        ModelAndView modelAndView = new ModelAndView("plans");
        modelAndView.addObject("gym", allGym);
        modelAndView.addObject("gymPlusTrainer", allGymPlusTrainer);

        return modelAndView;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/update-price")
    public String updateSubscriptionPrice(@PathVariable UUID id,
                                          @RequestParam BigDecimal newPrice) {

        subscriptionService.updatePrice(id, newPrice);

        return "redirect:/home-admin";
    }
}
