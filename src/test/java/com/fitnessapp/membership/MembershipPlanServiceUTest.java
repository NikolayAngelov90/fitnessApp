package com.fitnessapp.membership;

import com.fitnessapp.exception.BusinessException;
import com.fitnessapp.exception.ClientAlreadyHaveAnActiveMembershipException;
import com.fitnessapp.exception.MembershipPlanNotFoundException;
import com.fitnessapp.membership.event.UpsertMembershipEvent;
import com.fitnessapp.membership.model.MembershipPlan;
import com.fitnessapp.membership.model.MembershipPlanStatus;
import com.fitnessapp.membership.property.MembershipPlanProperty;
import com.fitnessapp.membership.repository.MembershipPlanRepository;
import com.fitnessapp.membership.service.MembershipPlanService;
import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.subscription.model.SubscriptionDuration;
import com.fitnessapp.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MembershipPlanServiceUTest {

    @Mock
    private MembershipPlanRepository membershipPlanRepository;
    @Mock
    private MembershipPlanProperty membershipPlanProperty;
    @Mock
    private KafkaTemplate<String, UpsertMembershipEvent> kafkaTemplate;
    @Captor
    private ArgumentCaptor<MembershipPlan> membershipPlanCaptor;
    @Captor
    private ArgumentCaptor<UpsertMembershipEvent> eventCaptor;

    @InjectMocks
    private MembershipPlanService membershipPlanService;

    @Test
    void givenNoActiveMembership_whenCreate_thenInitializeNewMembershipPlan() {
        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .build();

        Subscription subscription = Subscription.builder()
                .id(UUID.randomUUID())
                .duration(SubscriptionDuration.ONE_MONTH)
                .price(BigDecimal.valueOf(50.0))
                .build();

        when(membershipPlanRepository.findByClient(user)).thenReturn(Collections.emptyList());
        when(membershipPlanProperty.getDefaultStatus()).thenReturn(MembershipPlanStatus.ACTIVE);

        // When
        membershipPlanService.create(subscription, user);

        // Then
        verify(membershipPlanRepository).save(membershipPlanCaptor.capture());
        verify(kafkaTemplate).send(eq("membership-events"), eventCaptor.capture());

        MembershipPlan capturedPlan = membershipPlanCaptor.getValue();
        UpsertMembershipEvent capturedEvent = eventCaptor.getValue();

        assertEquals(user, capturedPlan.getClient());
        assertEquals(subscription, capturedPlan.getSubscription());
        assertEquals(LocalDate.now(), capturedPlan.getStartDate());
        assertEquals(LocalDate.now().plusMonths(1), capturedPlan.getEndDate());
        assertEquals(MembershipPlanStatus.ACTIVE, capturedPlan.getStatus());
        assertEquals(subscription.getPrice(), capturedPlan.getPrice());

        assertEquals(capturedPlan.getStartDate(), capturedEvent.getDate());
        assertEquals(capturedPlan.getStatus().toString(), capturedEvent.getType());
        assertEquals(capturedPlan.getPrice(), capturedEvent.getPrice());
    }

    @Test
    void givenExistingActiveMembership_whenCreate_thenThrowException() {
        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .build();

        Subscription subscription = Subscription.builder()
                .id(UUID.randomUUID())
                .duration(SubscriptionDuration.ONE_MONTH)
                .build();

        MembershipPlan activePlan = MembershipPlan.builder()
                .id(UUID.randomUUID())
                .client(user)
                .status(MembershipPlanStatus.ACTIVE)
                .build();

        when(membershipPlanRepository.findByClient(user)).thenReturn(List.of(activePlan));

        // When & Then
        assertThrows(ClientAlreadyHaveAnActiveMembershipException.class,
                () -> membershipPlanService.create(subscription, user));

        verify(membershipPlanRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), any(UpsertMembershipEvent.class));
    }

    @ParameterizedTest
    @MethodSource("subscriptionDurationArguments")
    void givenSubscriptionDuration_whenCalculateEndDate_thenReturnCorrectEndDate(SubscriptionDuration duration, LocalDate expectedEndDate) {
        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .build();

        Subscription subscription = Subscription.builder()
                .id(UUID.randomUUID())
                .duration(duration)
                .price(BigDecimal.valueOf(50.0))
                .build();

        when(membershipPlanRepository.findByClient(user)).thenReturn(Collections.emptyList());
        when(membershipPlanProperty.getDefaultStatus()).thenReturn(MembershipPlanStatus.ACTIVE);

        // When
        membershipPlanService.create(subscription, user);

        // Then
        verify(membershipPlanRepository).save(membershipPlanCaptor.capture());
        MembershipPlan capturedPlan = membershipPlanCaptor.getValue();

        assertEquals(expectedEndDate, capturedPlan.getEndDate());
    }

    @Test
    void givenActiveMembershipPlan_whenChangeStatus_thenPauseMembership() {
        // Given
        UUID planId = UUID.randomUUID();
        LocalDate now = LocalDate.now();
        LocalDate endDate = now.plusMonths(1);

        User user = User.builder()
                .email("test@example.com")
                .build();

        MembershipPlan activePlan = MembershipPlan.builder()
                .id(planId)
                .client(user)
                .status(MembershipPlanStatus.ACTIVE)
                .startDate(now)
                .endDate(endDate)
                .build();

        when(membershipPlanRepository.findById(planId)).thenReturn(Optional.of(activePlan));

        // When
        membershipPlanService.changeStatus(planId);

        // Then
        verify(membershipPlanRepository).save(membershipPlanCaptor.capture());
        MembershipPlan savedPlan = membershipPlanCaptor.getValue();

        assertEquals(MembershipPlanStatus.STOPPED, savedPlan.getStatus());
        assertEquals(now, savedPlan.getPausedDate());
        assertNotNull(savedPlan.getRemainingPeriod());
        assertEquals(Period.between(now, endDate), savedPlan.getRemainingPeriod());
    }

    @Test
    void givenStoppedMembershipPlan_whenChangeStatus_thenResumeMembership() {
        // Given
        UUID planId = UUID.randomUUID();
        LocalDate now = LocalDate.now();
        Period remainingPeriod = Period.ofDays(15);

        User user = User.builder()
                .email("test@example.com")
                .build();

        MembershipPlan stoppedPlan = MembershipPlan.builder()
                .id(planId)
                .client(user)
                .status(MembershipPlanStatus.STOPPED)
                .pausedDate(now.minusDays(5))
                .remainingPeriod(remainingPeriod)
                .build();

        when(membershipPlanRepository.findById(planId)).thenReturn(Optional.of(stoppedPlan));

        // When
        membershipPlanService.changeStatus(planId);

        // Then
        verify(membershipPlanRepository).save(membershipPlanCaptor.capture());
        MembershipPlan savedPlan = membershipPlanCaptor.getValue();

        assertEquals(MembershipPlanStatus.ACTIVE, savedPlan.getStatus());
        assertNull(savedPlan.getPausedDate());
        assertNull(savedPlan.getRemainingPeriod());
        assertEquals(now.plus(remainingPeriod), savedPlan.getEndDate());
    }

    @Test
    void givenActiveMembershipWithNoRemainingTime_whenChangeStatus_thenThrowException() {
        // Given
        UUID planId = UUID.randomUUID();
        LocalDate now = LocalDate.now();

        User user = User.builder()
                .email("test@example.com")
                .build();

        MembershipPlan activePlan = MembershipPlan.builder()
                .id(planId)
                .client(user)
                .status(MembershipPlanStatus.ACTIVE)
                .startDate(now.minusMonths(1))
                .endDate(now.minusDays(1))
                .build();

        when(membershipPlanRepository.findById(planId)).thenReturn(Optional.of(activePlan));

        // When & Then
        assertThrows(BusinessException.class, () -> membershipPlanService.changeStatus(planId));
    }

    @Test
    void givenNonExistentMembershipId_whenGetById_thenThrowException() {
        // Given
        UUID planId = UUID.randomUUID();
        when(membershipPlanRepository.findById(planId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MembershipPlanNotFoundException.class, () -> membershipPlanService.getById(planId));
    }

    @Test
    void givenExistingMembershipId_whenGetById_thenReturnMembershipPlan() {
        // Given
        UUID planId = UUID.randomUUID();
        MembershipPlan expectedPlan = MembershipPlan.builder()
                .id(planId)
                .build();

        when(membershipPlanRepository.findById(planId)).thenReturn(Optional.of(expectedPlan));

        // When
        MembershipPlan result = membershipPlanService.getById(planId);

        // Then
        assertEquals(expectedPlan, result);
    }

    @Test
    void givenCurrentDate_whenGetPlansToExpire_thenReturnOnlyActivePlansBeforeDate() {
        // Given
        LocalDate now = LocalDate.now();

        MembershipPlan activePlanToExpire1 = MembershipPlan.builder()
                .id(UUID.randomUUID())
                .status(MembershipPlanStatus.ACTIVE)
                .endDate(now.minusDays(1))
                .build();

        MembershipPlan activePlanToExpire2 = MembershipPlan.builder()
                .id(UUID.randomUUID())
                .status(MembershipPlanStatus.ACTIVE)
                .endDate(now.minusDays(2))
                .build();

        MembershipPlan stoppedPlanBeforeDate = MembershipPlan.builder()
                .id(UUID.randomUUID())
                .status(MembershipPlanStatus.STOPPED)
                .endDate(now.minusDays(1))
                .build();

        when(membershipPlanRepository.findByEndDateBefore(now))
                .thenReturn(List.of(activePlanToExpire1, activePlanToExpire2, stoppedPlanBeforeDate));

        // When
        List<MembershipPlan> result = membershipPlanService.getPlansToExpire(now);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(activePlanToExpire1));
        assertTrue(result.contains(activePlanToExpire2));
        assertFalse(result.contains(stoppedPlanBeforeDate));
    }

    @Test
    void givenMembershipPlan_whenSaveExpireMembership_thenSavePlan() {
        // Given
        MembershipPlan plan = MembershipPlan.builder()
                .id(UUID.randomUUID())
                .build();

        // When
        membershipPlanService.saveExpireMembership(plan);

        // Then
        verify(membershipPlanRepository).save(plan);
    }

    private static Stream<Arguments> subscriptionDurationArguments() {
        LocalDate now = LocalDate.now();
        return Stream.of(
                Arguments.of(SubscriptionDuration.ONE_MONTH, now.plusMonths(1)),
                Arguments.of(SubscriptionDuration.THREE_MONTHS, now.plusMonths(3)),
                Arguments.of(SubscriptionDuration.SIX_MONTHS, now.plusMonths(6)),
                Arguments.of(SubscriptionDuration.ONE_YEAR, now.plusYears(1))
        );
    }
}
