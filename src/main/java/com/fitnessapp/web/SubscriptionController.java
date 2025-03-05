package com.fitnessapp.web;

import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.subscription.service.SubscriptionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

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
}
