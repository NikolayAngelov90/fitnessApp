package com.fitnessapp.subscription.repository;

import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.subscription.model.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {


    List<Subscription> findAllByTypeOrderByPriceAsc(SubscriptionType type);

    List<Subscription> findAllByOrderByPriceAsc();
}
