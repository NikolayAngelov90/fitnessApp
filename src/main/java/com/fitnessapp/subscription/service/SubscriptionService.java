package com.fitnessapp.subscription.service;

import com.fitnessapp.exception.SubscriptionNotFoundException;
import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.subscription.model.SubscriptionType;
import com.fitnessapp.subscription.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public List<Subscription> getAllGymOnly() {
        return subscriptionRepository.findAllByTypeOrderByPriceAsc(SubscriptionType.GymOnly);
    }

    public List<Subscription> getAllGymPlusTrainer() {
        return subscriptionRepository.findAllByTypeOrderByPriceAsc(SubscriptionType.GymPlusTrainer);
    }

    public Subscription getById(UUID id) {
        return subscriptionRepository.findById(id).orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found!"));
    }
}
