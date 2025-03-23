package com.fitnessapp.scheduler;

import com.fitnessapp.membership.model.MembershipPlan;
import com.fitnessapp.membership.model.MembershipPlanStatus;
import com.fitnessapp.membership.service.MembershipPlanService;
import com.fitnessapp.membership.event.UpsertMembershipEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class MembershipExpireScheduler {

    private final MembershipPlanService membershipPlanService;
    private final KafkaTemplate<String, UpsertMembershipEvent> kafkaTemplate;

    public MembershipExpireScheduler(MembershipPlanService membershipPlanService,
                                     KafkaTemplate<String, UpsertMembershipEvent> kafkaTemplate) {
        this.membershipPlanService = membershipPlanService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(cron = "0 59 23 * * ?")
    public void expireOldMemberships() {

        List<MembershipPlan> plansToExpire = membershipPlanService.getPlansToExpire(LocalDate.now());

        if (plansToExpire.isEmpty()) {
            log.info("No membership plans expired");
            return;
        }

        plansToExpire.forEach(m -> {
            m.setStatus(MembershipPlanStatus.EXPIRED);
            membershipPlanService.saveExpireMembership(m);
            log.info("Membership plan  [{}] expired", m.getId());

            UpsertMembershipEvent event = UpsertMembershipEvent.builder()
                    .date(m.getEndDate())
                    .type(m.getStatus().toString())
                    .build();

            log.info("Start - Sending MembershipEvent {} to Kafka topic membership-events", event);
            kafkaTemplate.send("membership-events", event);
            log.info("End - Sending MembershipEvent {} to Kafka topic membership-events", event);
        });
    }
}
