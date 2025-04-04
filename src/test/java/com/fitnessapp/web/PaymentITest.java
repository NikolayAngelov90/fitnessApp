package com.fitnessapp.web;

import com.fitnessapp.payment.model.Payment;
import com.fitnessapp.payment.model.PaymentProductType;
import com.fitnessapp.payment.repository.PaymentRepository;
import com.fitnessapp.payment.service.PaymentService;
import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.subscription.model.SubscriptionDuration;
import com.fitnessapp.subscription.model.SubscriptionType;
import com.fitnessapp.subscription.repository.SubscriptionRepository;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.model.UserRole;
import com.fitnessapp.user.repository.UserRepository;
import com.fitnessapp.workout.model.RecurringType;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.model.WorkoutStatus;
import com.fitnessapp.workout.model.WorkoutType;
import com.fitnessapp.workout.repository.WorkoutRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class PaymentITest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @MockitoBean
    private SecurityContext securityContext;

    @Test
    void processSubscriptionPayment_happyPath() {
        // Given
        User client = createTestClient();
        User savedClient = userRepository.save(client);

        Subscription subscription = createTestSubscription();
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        mockSecurityContext(savedClient);

        // When
        paymentService.processMembershipPayment(savedSubscription, savedClient.getId());

        // Then
        savedClient = userRepository.findById(savedClient.getId()).orElseThrow();
        List<Payment> payments = paymentRepository.findByClient(savedClient);
        assertFalse(payments.isEmpty());

        Payment payment = payments.getFirst();
        assertEquals(savedClient.getId(), payment.getClient().getId());
        assertEquals(PaymentProductType.SUBSCRIPTION, payment.getType());
        assertEquals(savedSubscription.getId(), payment.getSubscription().getId());
        assertEquals(savedSubscription.getPrice(), payment.getAmount());
        assertEquals("succeeded", payment.getStatus());

        assertTrue(savedClient.getMemberships()
                .stream()
                .anyMatch(plan -> plan.getSubscription().getId().equals(savedSubscription.getId())));
    }

    @Test
    void processWorkoutPayment_happyPath() {
        // Given
        User client = createTestClient();
        User savedClient = userRepository.save(client);

        User trainer = createTestTrainer();
        User savedTrainer = userRepository.save(trainer);

        Workout workout = createTestWorkout(savedTrainer);
        Workout savedWorkout = workoutRepository.save(workout);

        mockSecurityContext(savedClient);

        // When
        paymentService.processWorkoutPayment(savedWorkout, savedClient.getId());

        // Then
        savedWorkout = workoutRepository.findById(savedWorkout.getId()).orElseThrow();
        List<Payment> payments = paymentRepository.findByClient(savedClient);
        assertFalse(payments.isEmpty());

        Payment payment = payments.getFirst();
        assertEquals(savedClient.getId(), payment.getClient().getId());
        assertEquals(PaymentProductType.WORKOUT, payment.getType());
        assertEquals(savedWorkout.getId(), payment.getWorkout().getId());
        assertEquals(savedWorkout.getPrice(), payment.getAmount());
        assertEquals("succeeded", payment.getStatus());

        assertFalse(savedWorkout.getClients().isEmpty());
        assertEquals(savedWorkout.getMaxParticipants() - 1, savedWorkout.getAvailableSpots());
    }

    private User createTestClient() {
        return User.builder()
                .firstName("Test")
                .lastName("Client")
                .email("test.client@example.com")
                .password("password")
                .role(UserRole.CLIENT)
                .registeredOn(LocalDateTime.now())
                .additionalTrainerDataCompleted(false)
                .memberships(new ArrayList<>())
                .build();
    }

    private User createTestTrainer() {
        return User.builder()
                .firstName("Test")
                .lastName("Trainer")
                .email("test.trainer@example.com")
                .password("password")
                .role(UserRole.TRAINER)
                .registeredOn(LocalDateTime.now())
                .additionalTrainerDataCompleted(true)
                .approveTrainer(true)
                .build();
    }

    private Subscription createTestSubscription() {
        return Subscription.builder()
                .type(SubscriptionType.GYM)
                .price(new BigDecimal("50.00"))
                .duration(SubscriptionDuration.ONE_MONTH)
                .build();
    }

    private Workout createTestWorkout(User trainer) {
        return Workout.builder()
                .workoutType(WorkoutType.YOGA)
                .duration(30)
                .price(new BigDecimal("25.00"))
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .recurringType(RecurringType.NONE)
                .trainer(trainer)
                .description("Yoga workout for beginners")
                .createdAt(LocalDateTime.now())
                .maxParticipants(10)
                .availableSpots(10)
                .status(WorkoutStatus.UPCOMING)
                .clients(new ArrayList<>())
                .build();
    }

    private void mockSecurityContext(User user) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.isAdditionalTrainerDataCompleted()));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
