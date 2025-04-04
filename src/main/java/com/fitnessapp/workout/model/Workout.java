package com.fitnessapp.workout.model;

import com.fitnessapp.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Entity
@Table(name = "workouts")
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkoutType workoutType;

    @Column(nullable = false)
    private int duration;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurringType recurringType;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User trainer;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private int maxParticipants;

    @Column(nullable = false)
    private int availableSpots;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkoutStatus status;

    @Column
    private UUID completedCloneWorkoutId;

    @Column
    private boolean nextRecurringCreated;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "clients_workouts")
    private List<User> clients = new ArrayList<>();
}
