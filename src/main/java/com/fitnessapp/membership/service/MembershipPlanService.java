package com.fitnessapp.membership.service;

import com.fitnessapp.exception.BusinessException;
import com.fitnessapp.exception.ClientAlreadyHaveAnActiveMembershipException;
import com.fitnessapp.exception.MembershipPlanNotFoundException;
import com.fitnessapp.membership.model.MembershipPlan;
import com.fitnessapp.membership.model.MembershipPlanStatus;
import com.fitnessapp.membership.property.MembershipPlanProperty;
import com.fitnessapp.membership.repository.MembershipPlanRepository;
import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.subscription.model.SubscriptionDuration;
import com.fitnessapp.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Slf4j
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

        List<MembershipPlan> clientMemberships = membershipPlanRepository.findByClient(user)
                .stream()
                .filter(m -> m.getStatus() == MembershipPlanStatus.ACTIVE)
                .toList();

        if (clientMemberships.isEmpty()) {
            initializeNewMembershipPlan(subscription, user);
        } else {
            throw new ClientAlreadyHaveAnActiveMembershipException(
                    "You have already an active membership plan", subscription.getId());
        }
    }

    public void changeStatus(UUID id) {

        MembershipPlan membershipPlan = getById(id);
        MembershipPlanStatus status = membershipPlan.getStatus();

        if (status == MembershipPlanStatus.ACTIVE) {
            pauseMembership(membershipPlan);
        } else if (status == MembershipPlanStatus.STOPPED) {
            resumeMembership(membershipPlan);
        }

        membershipPlanRepository.save(membershipPlan);
    }

    public MembershipPlan getById(UUID membershipId) {
        return membershipPlanRepository.findById(membershipId)
                .orElseThrow(() -> new MembershipPlanNotFoundException("Membership plan not found."));
    }

    private void initializeNewMembershipPlan(Subscription subscription, User user) {

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = calculateEndDate(startDate, subscription.getDuration());

        MembershipPlan membershipPlan = MembershipPlan.builder()
                .client(user)
                .subscription(subscription)
                .startDate(startDate)
                .endDate(endDate)
                .status(membershipPlanProperty.getDefaultStatus())
                .price(subscription.getPrice())
                .build();

        membershipPlanRepository.save(membershipPlan);

        log.info("Membership created for client [{}]", user.getFirstName() + " " + user.getLastName());
    }

    private LocalDate calculateEndDate(LocalDate startDate, SubscriptionDuration duration) {
        if (duration == null) {
            throw new IllegalArgumentException("Duration cannot be null");
        }

        return switch (duration) {
            case ONE_MONTH -> startDate.plusMonths(1);
            case THREE_MONTHS -> startDate.plusMonths(3);
            case SIX_MONTHS -> startDate.plusMonths(6);
            case ONE_YEAR -> startDate.plusYears(1);
        };
    }

    private void resumeMembership(MembershipPlan plan) {

        plan.setStatus(MembershipPlanStatus.ACTIVE);
        plan.setEndDate(LocalDate.now().plus(plan.getRemainingPeriod()));
        plan.setPausedDate(null);
        plan.setRemainingPeriod(null);
    }

    private void pauseMembership(MembershipPlan plan) {
        plan.setStatus(MembershipPlanStatus.STOPPED);
        plan.setPausedDate(LocalDate.now());

        Period remaining = Period.between(LocalDate.now(), plan.getEndDate());
        if (remaining.isZero() || remaining.isNegative()) {
            throw new BusinessException("Haven't enough remaining time!");
        }

        plan.setRemainingPeriod(remaining);
    }

    public List<MembershipPlan> getPlansToExpire(LocalDate now) {
        return membershipPlanRepository.findByEndDate(now)
                .stream()
                .filter(m -> m.getStatus() == MembershipPlanStatus.ACTIVE)
                .toList();
    }

    public void saveExpireMembership(MembershipPlan m) {
        membershipPlanRepository.save(m);
    }
}
