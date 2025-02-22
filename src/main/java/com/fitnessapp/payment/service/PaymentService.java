package com.fitnessapp.payment.service;

import com.fitnessapp.membership.service.MembershipPlanService;
import com.fitnessapp.payment.model.Payment;
import com.fitnessapp.payment.repository.PaymentRepository;
import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.user.model.User;
import com.fitnessapp.web.dto.PaymentRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MembershipPlanService membershipPlanService;


    public PaymentService(PaymentRepository paymentRepository,
                          MembershipPlanService membershipPlanService) {
        this.paymentRepository = paymentRepository;
        this.membershipPlanService = membershipPlanService;
    }

    @Transactional
    public void processMembershipPayment(Subscription subscription, User user, PaymentRequest paymentRequest) {

        Payment payment = Payment.builder()
                .client(user)
                .amount(subscription.getPrice())
                .dateTime(LocalDateTime.now())
                .subscription(subscription)
                .build();

        membershipPlanService.create(subscription, user);

        paymentRepository.save(payment);
    }
}
