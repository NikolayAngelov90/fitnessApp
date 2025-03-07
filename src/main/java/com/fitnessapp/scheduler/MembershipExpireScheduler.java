package com.fitnessapp.scheduler;

import com.fitnessapp.membership.model.MembershipPlan;
import com.fitnessapp.membership.model.MembershipPlanStatus;
import com.fitnessapp.membership.service.MembershipPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class MembershipExpireScheduler {

    private final MembershipPlanService membershipPlanService;

    public MembershipExpireScheduler(MembershipPlanService membershipPlanService) {
        this.membershipPlanService = membershipPlanService;
    }

    @Scheduled(cron = "0 59 23 * * ?")
    public void expireOldMemberships() {

        List<MembershipPlan> plansToExpire = membershipPlanService.getPlansToExpire(LocalDate.now());

        if (plansToExpire.isEmpty()) {
            log.info("No membership plans expired");
        }

        plansToExpire.forEach(m -> {
            m.setStatus(MembershipPlanStatus.EXPIRED);
            membershipPlanService.saveExpireMembership(m);
            log.info("Membership plan  [{}] expired", m.getId());
        });
    }
}
