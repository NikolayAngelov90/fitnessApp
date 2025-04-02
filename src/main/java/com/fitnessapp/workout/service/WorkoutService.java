package com.fitnessapp.workout.service;

import com.fitnessapp.exception.*;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.web.dto.WorkoutRequest;
import com.fitnessapp.workout.model.RecurringType;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.model.WorkoutStatus;
import com.fitnessapp.workout.model.WorkoutType;
import com.fitnessapp.workout.property.WorkoutProperty;
import com.fitnessapp.workout.repository.WorkoutRepository;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final WorkoutProperty workoutProperty;
    private final UserService userService;

    public WorkoutService(WorkoutRepository workoutRepository,
                          WorkoutProperty workoutProperty,
                          UserService userService) {
        this.workoutRepository = workoutRepository;
        this.workoutProperty = workoutProperty;
        this.userService = userService;
    }

    public void registerClient(Workout workout, User client) {

        validateRegistration(client, workout);

        workout.getClients().add(client);
        workout.setAvailableSpots(workout.getAvailableSpots() - 1);

        if (workout.getAvailableSpots() == 0) {
            workout.setStatus(WorkoutStatus.FULL);
        }

        workoutRepository.save(workout);
        log.info("Client {} successfully registered for  workout id [{}]", client.getEmail(), workout.getId());
    }

    public void cancelBookingWorkout(UUID workoutId, UUID clientId) {

        Workout workout = getById(workoutId);
        User client = userService.getById(clientId);

        if (workout.getStatus() == WorkoutStatus.COMPLETED) {
            throw new CancelBookedWorkoutException("Can't cancel booked workout");
        }

        workout.getClients().remove(client);
        workout.setAvailableSpots(workout.getAvailableSpots() + 1);

        workoutRepository.save(workout);
        log.info("Client {} has been cancelled workout with id [{}]", client.getEmail(), workoutId);
    }

    public List<Workout> getAllRegisteredClientWorkouts(UUID clientId) {
        User user = userService.getById(clientId);

        return workoutRepository.findAllByClientsOrderByEndTimeDesc(List.of(user));
    }

    public void create(WorkoutRequest workoutRequest, UUID trainerId) {

        User trainer = userService.getById(trainerId);

        if (!trainer.isApproveTrainer()) {
            throw new TrainerNotApproveException("You haven't permission to create workout");

        }

        Workout workout = Workout.builder()
                .workoutType(workoutRequest.workoutType())
                .duration(workoutRequest.duration())
                .price(workoutRequest.price())
                .startTime(workoutRequest.startTime())
                .endTime(workoutRequest.startTime().plusMinutes(workoutRequest.duration()))
                .recurringType(workoutRequest.recurringType())
                .trainer(trainer)
                .description(workoutRequest.description().trim().replaceAll("\\s+", " "))
                .createdAt(LocalDateTime.now())
                .maxParticipants(workoutRequest.maxParticipants())
                .availableSpots(workoutRequest.maxParticipants())
                .status(workoutProperty.getDefaultStatus())
                .build();

        workoutRepository.save(workout);
        log.info("Created workout with id [{}] from trainer {}", workout.getId(), trainer.getEmail());
    }

    public void edit(Workout workout, WorkoutRequest workoutRequest) {

        updatedWorkout(workoutRequest, workout);

        workoutRepository.save(workout);
        log.info("Workout with id [{}] was updated successfully", workout.getId());
    }

    public List<Workout> getAllWorkouts() {

        List<Workout> workouts = workoutRepository.findAll();
        if (workouts.isEmpty()) {
            throw new WorkoutNotFoundException("Workout not found");
        }

        return workouts;
    }

    public List<Workout> getAllDisplayedWorkouts(LocalDate today,
                                                 WorkoutType workoutType,
                                                 UUID trainerId,
                                                 LocalDate date,
                                                 String timeRange) {

        boolean hasFilters = workoutType != null || trainerId != null || date != null ||
                (timeRange != null && !timeRange.isEmpty());

        if (!hasFilters) {
            return workoutRepository.findWorkoutsForDisplay(today);
        } else {
            Specification<Workout> spec = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                Predicate statusCondition =
                        criteriaBuilder.or(root.get("status").in(Arrays.asList("UPCOMING", "FULL")),
                                criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), "COMPLETED"),
                                        criteriaBuilder.equal(criteriaBuilder.function("DATE", Date.class, root.get("endTime")), today.atStartOfDay())
                                ));

                predicates.add(statusCondition);

                if (workoutType != null) {
                    predicates.add(criteriaBuilder.equal(root.get("workoutType"), workoutType));
                }

                if (trainerId != null) {
                    predicates.add(criteriaBuilder.equal(root.join("trainer").get("id"), trainerId));
                }

                if (date != null) {
                    predicates.add(criteriaBuilder.equal(
                            criteriaBuilder.function("DATE", Date.class, root.get("startTime")), date.atStartOfDay()));
                }

                if (timeRange != null && !timeRange.isEmpty()) {
                    Expression<Integer> hour = criteriaBuilder.function("HOUR", Integer.class, root.get("startTime"));

                    switch (timeRange) {
                        case "MORNING":
                            predicates.add(criteriaBuilder.between(hour, 6, 11));
                            break;
                        case "AFTERNOON":
                            predicates.add(criteriaBuilder.between(hour, 12, 16));
                            break;
                        case "EVENING":
                            predicates.add(criteriaBuilder.between(hour, 17, 21));
                            break;
                    }
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };

            return workoutRepository.findAll(spec);
        }


    }

    public Workout getById(UUID id) {
        return workoutRepository.findById(id).orElseThrow(() -> new WorkoutNotFoundException("Workout not found"));
    }

    public void saveCompletedWorkouts(Workout workout) {
        workoutRepository.save(workout);
    }

    public List<Workout> getAllCompletedRecurringWorkouts() {
        return workoutRepository.findAllByStatusAndRecurringTypeNotAndNextRecurringCreatedFalse(
                WorkoutStatus.COMPLETED,
                RecurringType.NONE);
    }

    public List<Workout> getUpcomingWorkoutsByTrainer(User trainer) {
        return workoutRepository.findAllByTrainerAndStatusOrderByStartTime(trainer, WorkoutStatus.UPCOMING);
    }

    @Transactional
    public void saveRecurringWorkouts(Workout newWorkout) {
        workoutRepository.save(newWorkout);

        UUID hasGeneratedNextWorkout = newWorkout.getCompletedCloneWorkoutId();
        Workout completedWorkout = getById(hasGeneratedNextWorkout);
        completedWorkout.setNextRecurringCreated(true);
        workoutRepository.save(completedWorkout);

    }

    public void changeStatusDeleted(UUID id) {
        Workout workout = getById(id);
        workout.setStatus(WorkoutStatus.DELETED);

        workoutRepository.save(workout);
        log.info("Workout with id [{}] was soft deleted successfully", workout.getId());
    }

    public List<Workout> getAllWorkoutsByTrainer(UUID trainerId) {

        User trainer = userService.getById(trainerId);

        return workoutRepository.findAllByTrainerOrderByStartTimeDesc(trainer);
    }

    public List<Workout> getMonthCompletedWorkoutsByTrainer(User trainer) {

        return workoutRepository.findAllByTrainerAndStatusOrderByStartTime(trainer, WorkoutStatus.COMPLETED)
                .stream()
                .filter(w -> {
                    LocalDateTime workoutStartTime = w.getStartTime();
                    LocalDateTime now = LocalDateTime.now();

                    return workoutStartTime.getYear() == now.getYear() &&
                            workoutStartTime.getMonth() == now.getMonth();
                })
                .toList();
    }

    public double calculateMonthlyAttendancePercentage(User trainer) {

        List<Workout> monthCompletedWorkouts = getMonthCompletedWorkoutsByTrainer(trainer);

        if (monthCompletedWorkouts.isEmpty()) {
            return 0.0;
        }

        double totalPercentage = monthCompletedWorkouts
                .stream()
                .mapToDouble(w -> {
                    int maxParticipants = w.getMaxParticipants();
                    int registeredClients = w.getClients().size();

                    return (double) registeredClients / maxParticipants * 100;
                })
                .sum();

        return totalPercentage / monthCompletedWorkouts.size();
    }

    public int getAllCompletedMonthWorkoutsForClient(User client) {

        return (int) getAllRegisteredClientWorkouts(client.getId())
                .stream()
                .filter(w -> w.getStatus() == WorkoutStatus.COMPLETED)
                .filter(w -> {
                    LocalDateTime workoutStartTime = w.getStartTime();
                    LocalDateTime now = LocalDateTime.now();

                    return workoutStartTime.getYear() == now.getYear() &&
                            workoutStartTime.getMonth() == now.getMonth();
                })
                .count();
    }

    public int getAllCompletedMonthWorkouts() {

        return (int) getAllWorkouts()
                .stream()
                .filter(w -> {
                    LocalDateTime workoutStartTime = w.getStartTime();
                    LocalDateTime now = LocalDateTime.now();

                    return workoutStartTime.getYear() == now.getYear() &&
                            workoutStartTime.getMonth() == now.getMonth();
                })
                .count();
    }

    public void saveDefaultTrainerWorkout(Workout workout) {
        workoutRepository.save(workout);
    }

    private void validateRegistration(User client, Workout workout) {
        if (workout.getStatus() == WorkoutStatus.FULL) {
            throw new WorkoutFullException("Workout is already full");
        }

        if (workout.getClients().contains(client)) {
            throw new DuplicateRegistrationClientWorkout("You already book this workout");
        }
    }

    private void updatedWorkout(WorkoutRequest workoutRequest, Workout workout) {
        workout.setWorkoutType(workoutRequest.workoutType());
        workout.setDuration(workoutRequest.duration());
        workout.setPrice(workoutRequest.price());
        workout.setStartTime(workoutRequest.startTime());
        workout.setEndTime(workoutRequest.startTime().plusMinutes(workoutRequest.duration()));
        workout.setRecurringType(workoutRequest.recurringType());
        workout.setDescription(workoutRequest.description());

        if (workout.getMaxParticipants() != workoutRequest.maxParticipants()) {
            int oldMaxParticipants = workout.getMaxParticipants();
            int newMaxParticipants = workoutRequest.maxParticipants();
            int diffCountSpots = Math.abs(newMaxParticipants - oldMaxParticipants);

            workout.setMaxParticipants(newMaxParticipants);

            if (newMaxParticipants > oldMaxParticipants) {
                workout.setAvailableSpots(workout.getAvailableSpots() + diffCountSpots);
            } else {
                workout.setAvailableSpots(workout.getAvailableSpots() - diffCountSpots);
            }
        }
    }
}
