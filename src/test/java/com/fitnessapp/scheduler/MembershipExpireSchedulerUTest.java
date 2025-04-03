package com.fitnessapp.scheduler;

import com.fitnessapp.membership.event.UpsertMembershipEvent;
import com.fitnessapp.membership.model.MembershipPlan;
import com.fitnessapp.membership.model.MembershipPlanStatus;
import com.fitnessapp.membership.service.MembershipPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MembershipExpireSchedulerUTest {

    @Mock
    private MembershipPlanService membershipPlanService;
    @Mock
    private KafkaTemplate<String, UpsertMembershipEvent> kafkaTemplate;
    @Captor
    private ArgumentCaptor<UpsertMembershipEvent> eventCaptor;
    @Captor
    private ArgumentCaptor<MembershipPlan> planCaptor;
    @Captor
    private ArgumentCaptor<String> topicCaptor;

    @InjectMocks
    private MembershipExpireScheduler membershipExpireScheduler;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
    }

    @Test
    void givenNoPlansToExpire_whenExpireOldMemberships_thenNoActionsTaken() {
        // Given
        when(membershipPlanService.getPlansToExpire(any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        membershipExpireScheduler.expireOldMemberships();

        // Then
        verify(membershipPlanService, never()).saveExpireMembership(any(MembershipPlan.class));
        verify(kafkaTemplate, never()).send(anyString(), any(UpsertMembershipEvent.class));
    }

    @Test
    void givenOnePlanExpires_whenExpireOldMemberships_thenPlanSavedAsExpiredAndEventSent() {
        // Given
        MembershipPlan planToExpire = MembershipPlan.builder()
                .id(UUID.randomUUID())
                .endDate(today)
                .status(MembershipPlanStatus.ACTIVE)
                .build();

        when(membershipPlanService.getPlansToExpire(any(LocalDate.class)))
                .thenReturn(List.of(planToExpire));

        // When
        membershipExpireScheduler.expireOldMemberships();

        // Then
        verify(membershipPlanService, times(1)).saveExpireMembership(planCaptor.capture());
        MembershipPlan savedPlan = planCaptor.getValue();
        assertNotNull(savedPlan);
        assertEquals(planToExpire.getId(), savedPlan.getId());
        assertEquals(MembershipPlanStatus.EXPIRED, savedPlan.getStatus());

        verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), eventCaptor.capture());
        String capturedTopic = topicCaptor.getValue();
        UpsertMembershipEvent capturedEvent = eventCaptor.getValue();
        assertEquals("membership-events", capturedTopic);
        assertNotNull(capturedEvent);
        assertEquals(today, capturedEvent.getDate());
        assertEquals(MembershipPlanStatus.EXPIRED.toString(), capturedEvent.getType());
    }

    @Test
    void givenMultiplePlansExpire_whenExpireOldMemberships_thenAllPlansSavedAndEventsSent() {
        // Given
        MembershipPlan plan1 = MembershipPlan.builder()
                .id(UUID.randomUUID())
                .endDate(today)
                .status(MembershipPlanStatus.ACTIVE)
                .build();

        MembershipPlan plan2 = MembershipPlan.builder()
                .id(UUID.randomUUID())
                .endDate(today)
                .status(MembershipPlanStatus.ACTIVE)
                .build();

        List<MembershipPlan> plansToExpire = List.of(plan1, plan2);
        when(membershipPlanService.getPlansToExpire(any(LocalDate.class)))
                .thenReturn(plansToExpire);

        // When
        membershipExpireScheduler.expireOldMemberships();

        // Then
        verify(membershipPlanService, times(2)).saveExpireMembership(planCaptor.capture());
        List<MembershipPlan> savedPlans = planCaptor.getAllValues();
        assertEquals(2, savedPlans.size());
        assertTrue(savedPlans.stream().allMatch(p -> p.getStatus() == MembershipPlanStatus.EXPIRED));
        assertTrue(savedPlans.stream().anyMatch(p -> p.getId().equals(plan1.getId())));
        assertTrue(savedPlans.stream().anyMatch(p -> p.getId().equals(plan2.getId())));

        verify(kafkaTemplate, times(2)).send(topicCaptor.capture(), eventCaptor.capture());
        List<String> capturedTopics = topicCaptor.getAllValues();
        List<UpsertMembershipEvent> capturedEvents = eventCaptor.getAllValues();
        assertEquals(2, capturedTopics.size());
        assertTrue(capturedTopics.stream().allMatch(t -> t.equals("membership-events")));
        assertEquals(2, capturedEvents.size());
        assertTrue(capturedEvents.stream().allMatch(e -> e.getType().equals(MembershipPlanStatus.EXPIRED.toString())));
        assertTrue(capturedEvents.stream().allMatch(e -> e.getDate().equals(today)));
    }
}
