package com.fitnessapp.membership.repository;

import com.fitnessapp.membership.model.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<MembershipPlan, UUID> {
}
