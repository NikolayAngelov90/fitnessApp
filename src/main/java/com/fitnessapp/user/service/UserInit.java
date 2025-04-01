package com.fitnessapp.user.service;

import  com.fitnessapp.user.model.User;
import com.fitnessapp.user.model.UserRole;
import com.fitnessapp.user.repository.UserRepository;
import com.fitnessapp.workout.model.RecurringType;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.model.WorkoutStatus;
import com.fitnessapp.workout.model.WorkoutType;
import com.fitnessapp.workout.service.WorkoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Slf4j
public class UserInit implements CommandLineRunner {

    public static final String TRAINER_SPECIALIZATION = "Crossfit and strength training";
    public static final String TRAINER_DESCRIPTION = "Certified trainer with over 10 years of experience, specializing in designing personalized training programs that combine cardio and strength exercises for optimal results.";
    public static final String WORKOUT_DESCRIPTION = "CrossFit is a high-intensity functional training program that combines elements from various sports and exercises.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WorkoutService workoutService;

    public UserInit(UserRepository userRepository,
                    PasswordEncoder passwordEncoder,
                    WorkoutService workoutService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.workoutService = workoutService;
    }

    @Override
    public void run(String... args) {

        if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {

            User admin = User.builder()
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("ADMIN123"))
                    .role(UserRole.ADMIN)
                    .registeredOn(LocalDateTime.now())
                    .build();

            userRepository.save(admin);
            log.info("Default admin account created: email-[admin@gmail.com], password-[ADMIN123]");
        }

        if (userRepository.findByEmail("trainer@gmail.com").isEmpty()) {

            User trainer = User.builder()
                    .firstName("Trainer")
                    .lastName("Test")
                    .email("trainer@gmail.com")
                    .password(passwordEncoder.encode("TRAINER123"))
                    .phoneNumber("+35921234567")
                    .role(UserRole.TRAINER)
                    .registeredOn(LocalDateTime.now())
                    .specialization(TRAINER_SPECIALIZATION)
                    .description(TRAINER_DESCRIPTION)
                    .additionalTrainerDataCompleted(true)
                    .approveTrainer(true)
                    .build();

            userRepository.save(trainer);
            log.info("Default trainer account created: email-[trainer@gmail.com], password-[TRAINER123]");

            LocalDateTime startTime = LocalDateTime.now().plusMinutes(10);
            Workout workout = Workout.builder()
                    .workoutType(WorkoutType.CROSS_FIT)
                    .duration(40)
                    .price(BigDecimal.valueOf(30))
                    .startTime(startTime)
                    .endTime(startTime.plusMinutes(40))
                    .recurringType(RecurringType.DAILY)
                    .trainer(trainer)
                    .description(WORKOUT_DESCRIPTION)
                    .createdAt(LocalDateTime.now())
                    .maxParticipants(15)
                    .availableSpots(5)
                    .status(WorkoutStatus.UPCOMING)
                    .build();

            workoutService.saveDefaultTrainerWorkout(workout);
            log.info("Default workout for default trainer [trainer@gmail.com] has been created");
        }
    }
}
