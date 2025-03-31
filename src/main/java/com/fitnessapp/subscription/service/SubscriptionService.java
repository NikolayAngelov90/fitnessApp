package com.fitnessapp.subscription.service;

import com.fitnessapp.exception.SubscriptionNotFoundException;
import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.subscription.model.SubscriptionType;
import com.fitnessapp.subscription.repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public List<Subscription> getAllGym() {
        return subscriptionRepository.findAllByTypeOrderByPriceAsc(SubscriptionType.GYM);
    }

    public List<Subscription> getAllGymPlusTrainer() {
        return subscriptionRepository.findAllByTypeOrderByPriceAsc(SubscriptionType.GYM_PLUS_TRAINER);
    }

    public Subscription getById(UUID id) {
        return subscriptionRepository.findById(id).orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found!"));
    }

    public List<Subscription> getAll() {
        return subscriptionRepository.findAll();
    }

    public void updatePrice(UUID id, BigDecimal newPrice) {

        Subscription subscription = getById(id);
        subscription.setPrice(newPrice);

        subscriptionRepository.save(subscription);
        log.info("Subscription with id [{}] has been updated with price [{}]", subscription.getId(), newPrice);
    }
}
