package com.fitnessapp.user.model;

import com.fitnessapp.membership.model.MembershipPlan;
import com.fitnessapp.workout.model.Workout;
import jakarta.persistence.*;
import lombok.*;

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
    @Column
    private byte[] profilePicture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private LocalDateTime registeredOn;

    @OneToMany(mappedBy = "client")
    private List<MembershipPlan> subscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "trainer")
    private List<Workout> trainerWorkouts = new ArrayList<>();

}
