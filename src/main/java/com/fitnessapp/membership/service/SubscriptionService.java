package com.fitnessapp.membership.service;

import com.fitnessapp.membership.repository.SubscriptionRepository;
import com.fitnessapp.membership.repository.UserSubscriptionRepository;
import com.fitnessapp.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserService userService;


    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               UserSubscriptionRepository userSubscriptionRepository,
                               UserService userService) {
        this.subscriptionRepository = subscriptionRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.userService = userService;
    }


}
