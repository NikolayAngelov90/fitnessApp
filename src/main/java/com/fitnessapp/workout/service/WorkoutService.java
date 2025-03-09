package com.fitnessapp.workout.service;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

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
    }

    public void cancelBookingWorkout(UUID workoutId, UUID clientId) {

        Workout workout = getById(workoutId);
        User user = userService.getById(clientId);

        workout.getClients().remove(user);
        workout.setAvailableSpots(workout.getAvailableSpots() + 1);

        workoutRepository.save(workout);
    }

    public List<Workout> getAllRegisteredClientWorkouts(UUID clientId) {
        User user = userService.getById(clientId);

        return workoutRepository.findAllByClients(List.of(user), Limit.of(1));
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

        log.info("Created workout with id {} from trainer {} {}",
                workout.getId(),
                trainer.getFirstName(),
                trainer.getLastName());
    }

    public List<Workout> getAllWorkouts() {

        List<Workout> workouts = workoutRepository.findAll();
        if (workouts.isEmpty()) {
            throw new WorkoutNotFoundException("Workout not found");
        }

        return workouts;
    }

    public Workout getById(UUID id) {
        return workoutRepository.findById(id).orElseThrow(() -> new WorkoutNotFoundException("Workout not found"));
    }

    public void saveWorkouts(Workout workout) {
        workoutRepository.save(workout);
    }

    public List<Workout> getAllCompletedRecurringWorkouts() {
        return workoutRepository.findAllByStatus(WorkoutStatus.COMPLETED)
                .stream()
                .filter(w -> w.getRecurringType() != RecurringType.NONE)
                .toList();
    }

    private void validateRegistration(User client, Workout workout) {
        if (workout.getStatus() == WorkoutStatus.FULL) {
            throw new WorkoutFullException("Workout is already full");
        }

        if (workout.getClients().contains(client)) {
            throw new DuplicateRegistrationClientWorkout("You already book this workout", workout.getId());
        }
    }
}
