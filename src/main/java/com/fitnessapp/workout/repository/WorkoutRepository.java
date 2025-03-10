package com.fitnessapp.workout.repository;

import com.fitnessapp.user.model.User;
import com.fitnessapp.workout.model.RecurringType;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.model.WorkoutStatus;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, UUID> {

    List<Workout> findAllByStatusAndRecurringTypeNotAndNextRecurringCreatedFalse(WorkoutStatus status, RecurringType recurringType);

    List<Workout> findAllByClients(List<User> clients, Limit limit);

    @Query("SELECT w FROM Workout w WHERE w.status IN ('UPCOMING', 'FULL') OR (w.status = 'COMPLETED' AND function('DATE', w.endTime) = :today)")
    List<Workout> findWorkoutsForDisplay(@Param("today") LocalDate today);

    List<Workout> findAllByTrainerAndOriginalWorkoutIsNull(User trainer);
}
