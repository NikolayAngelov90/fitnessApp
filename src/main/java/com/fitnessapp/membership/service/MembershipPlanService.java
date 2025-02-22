package com.fitnessapp.membership.service;

import com.fitnessapp.membership.model.MembershipPlan;
import com.fitnessapp.membership.property.MembershipPlanProperty;
import com.fitnessapp.membership.repository.MembershipPlanRepository;
import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.user.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class MembershipPlanService {

    private final MembershipPlanRepository membershipPlanRepository;
    private final MembershipPlanProperty membershipPlanProperty;

    public MembershipPlanService(MembershipPlanRepository membershipPlanRepository,
                                 MembershipPlanProperty membershipPlanProperty) {
        this.membershipPlanRepository = membershipPlanRepository;
        this.membershipPlanProperty = membershipPlanProperty;
    }

    public void create(Subscription subscription, User user) {

        MembershipPlan membershipPlan = MembershipPlan.builder()
                .client(user)
                .subscription(subscription)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .status(membershipPlanProperty.getDefaultStatus())
                .price(subscription.getPrice())
                .build();

        membershipPlanRepository.save(membershipPlan);
    }
}
