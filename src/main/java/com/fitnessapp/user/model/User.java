package com.fitnessapp.user.model;

import com.fitnessapp.membership.model.MembershipPlan;
import com.fitnessapp.payment.model.Payment;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@DynamicUpdate
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] profilePicture;

    @Column(unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private LocalDateTime registeredOn;

    @Column
    private String specialization;

    @Column
    private String description;

    @Column
    private boolean additionalTrainerDataCompleted;

    @Column
    private boolean approveTrainer;

    @OneToMany(mappedBy = "client")
    @OrderBy("startDate DESC")
    private List<MembershipPlan> memberships = new ArrayList<>();

    @OneToMany(mappedBy = "client")
    @OrderBy("dateTime DESC")
    private List<Payment> payments = new ArrayList<>();
}
