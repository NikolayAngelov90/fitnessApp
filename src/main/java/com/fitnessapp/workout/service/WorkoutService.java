package com.fitnessapp.workout.service;

import com.fitnessapp.exception.CancelBookedWorkoutException;
import com.fitnessapp.exception.DuplicateRegistrationClientWorkout;
import com.fitnessapp.exception.WorkoutFullException;
import com.fitnessapp.exception.WorkoutNotFoundException;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.web.dto.WorkoutRequest;
import com.fitnessapp.workout.model.RecurringType;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.model.WorkoutStatus;
import com.fitnessapp.workout.property.WorkoutProperty;
import com.fitnessapp.workout.repository.WorkoutRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

        Workout workout = Workout.builder()
                .workoutType(workoutRequest.workoutType())
                .duration(workoutRequest.duration())
                .price(workoutRequest.price())
                .startTime(workoutRequest.startTime())
                .endTime(workoutRequest.startTime().plusMinutes(workoutRequest.duration()))
                .recurringType(workoutRequest.recurringType())
                .trainer(trainer)
                .description(workoutRequest.description())
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

    public List<Workout> getAllDisplayedWorkouts(LocalDate today) {
        return workoutRepository.findWorkoutsForDisplay(today);
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

    public List<Workout> getAllWorkoutsByTrainer(UUID trainerId) {

        User trainer = userService.getById(trainerId);

        return workoutRepository.findAllByTrainerOrderByStartTimeDesc(trainer);
    }
}
