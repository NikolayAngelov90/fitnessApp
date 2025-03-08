package com.fitnessapp.payment.service;

import com.fitnessapp.exception.PaymentFailedException;
import com.fitnessapp.exception.PaymentNotFoundException;
import com.fitnessapp.membership.service.MembershipPlanService;
import com.fitnessapp.payment.model.Payment;
import com.fitnessapp.payment.model.PaymentProductType;
import com.fitnessapp.payment.property.StripeTestProperty;
import com.fitnessapp.payment.repository.PaymentRepository;
import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.subscription.service.SubscriptionService;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.service.WorkoutService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PaymentService {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    private final PaymentRepository paymentRepository;
    private final MembershipPlanService membershipPlanService;
    private final SubscriptionService subscriptionService;
    private final WorkoutService workoutService;
    private final UserService userService;
    private final StripeTestProperty stripeTestProperty;


    public PaymentService(PaymentRepository paymentRepository,
                          MembershipPlanService membershipPlanService,
                          SubscriptionService subscriptionService,
                          WorkoutService workoutService,
                          UserService userService,
                          StripeTestProperty stripeTestProperty) {
        this.paymentRepository = paymentRepository;
        this.membershipPlanService = membershipPlanService;
        this.subscriptionService = subscriptionService;
        this.workoutService = workoutService;
        this.userService = userService;
        this.stripeTestProperty = stripeTestProperty;
    }

    public void processMembershipPayment(Subscription subscription, UUID userId) {

        User user = userService.getById(userId);
        processPayment(PaymentProductType.SUBSCRIPTION, subscription.getId(), user);
    }

    public void processWorkoutPayment(Workout workout, UUID userId) {

        User user = userService.getById(userId);
        processPayment(PaymentProductType.WORKOUT, workout.getId(), user);
    }

    public List<Payment> getAllUserPayments(UUID userId) {

        User user = userService.getById(userId);

        List<Payment> allByClient = paymentRepository.findAllByClientOrderByDateTimeDesc(user);
        if (allByClient.isEmpty()) {
            throw new PaymentNotFoundException("Payments not found.");
        }

        return allByClient;
    }

    public void processPayment(PaymentProductType productType, UUID productId, User user) {

        BigDecimal price;
        Object product;
        switch (productType) {
            case SUBSCRIPTION -> {
                Subscription subscription = subscriptionService.getById(productId);
                price = subscription.getPrice();
                product = subscription;
            }
            case WORKOUT -> {
                Workout workout = workoutService.getById(productId);
                price = workout.getPrice();
                product = workout;
            }
            default -> throw new IllegalArgumentException("Invalid product type");
        }

        try {
            processSuccessfulPayment(productType, product, price, user);
        } catch (StripeException e) {
            saveFailedPayment(productType, product, price, user, e);
            throw new PaymentFailedException("Payment failed: " + e.getMessage(), productId, productType);
        } catch (IndexOutOfBoundsException e) {
            log.error("Index out of bounds error while processing payment: [{}]", e.getMessage());
            throw new PaymentFailedException("Payment failed due to system error", productId, productType);
        }

    }

    private void processSuccessfulPayment(PaymentProductType productType, Object product, BigDecimal price, User user) throws StripeException {

        Stripe.apiKey = stripeSecretKey;

        PaymentIntentCreateParams paymentParams = createPaymentParams(price);
        PaymentIntent paymentIntent = PaymentIntent.create(paymentParams);

        Payment.PaymentBuilder paymentBuilder = Payment.builder()
                .client(user)
                .amount(price)
                .dateTime(LocalDateTime.now())
                .type(productType)
                .status(paymentIntent.getStatus())
                .transactionId(paymentIntent.getId());

        if (productType == PaymentProductType.SUBSCRIPTION) {
            paymentBuilder.subscription((Subscription) product);
        } else if (productType == PaymentProductType.WORKOUT) {
            paymentBuilder.workout((Workout) product);
        }

        Payment payment = paymentBuilder.build();

        activateProduct(productType, product, user);

        paymentRepository.save(payment);
        log.info("Payment created with id: [{}], status: [{}], type: [{}]",
                payment.getId(), payment.getStatus(), productType);
    }

    private void saveFailedPayment(PaymentProductType productType, Object product, BigDecimal price, User user, StripeException e) {

        String errorCode = e.getStripeError() != null ? e.getStripeError().getCode() : "unknown";
        String errorMessage = e.getMessage();

        Payment.PaymentBuilder failedPaymentBuilder = Payment.builder()
                .client(user)
                .amount(price)
                .dateTime(LocalDateTime.now())
                .type(productType)
                .status("failed")
                .errorCode(errorCode)
                .errorMessage(errorMessage);

        if (productType == PaymentProductType.SUBSCRIPTION) {
            failedPaymentBuilder.subscription((Subscription) product);
        } else if (productType == PaymentProductType.WORKOUT) {
            failedPaymentBuilder.workout((Workout) product);
        }

        Payment failedPayment = failedPaymentBuilder.build();
        Payment saved = paymentRepository.save(failedPayment);

        log.error("Payment rejected with id: [{}], error: [{}], code: [{}], type: [{}]",
                saved.getId(), errorMessage, errorCode, productType);
    }

    private void activateProduct(PaymentProductType productType, Object product, User user) {

        switch (productType) {
            case SUBSCRIPTION:
                membershipPlanService.create((Subscription) product, user);
                break;
            case WORKOUT:
                workoutService.registerClient((Workout) product, user);
                break;
        }
    }

    private PaymentIntentCreateParams createPaymentParams(BigDecimal price) {
        PaymentIntentCreateParams.AutomaticPaymentMethods automaticPaymentMethods =
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                        .build();

        return PaymentIntentCreateParams.builder()
                .setAmount(convertToCents(price))
                .setCurrency("eur")
                .setPaymentMethod(stripeTestProperty.getToken())
                .setAutomaticPaymentMethods(automaticPaymentMethods)
                .setConfirm(true)
                .build();
    }

    private static long convertToCents(BigDecimal price) {
        return price
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }
}
