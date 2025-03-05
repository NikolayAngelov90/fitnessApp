package com.fitnessapp.payment.model;

import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.user.model.User;
import com.fitnessapp.workout.model.Workout;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User client;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @ManyToOne
    @JoinColumn
    private Subscription subscription;

    @ManyToOne
    @JoinColumn
    private Workout workout;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentProductType type;

    @Column(nullable = false)
    private String status;

    @Column
    private String transactionId;

    @Column
    private String errorCode;

    @Column
    private String errorMessage;
}
