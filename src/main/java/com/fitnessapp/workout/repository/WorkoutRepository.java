package com.fitnessapp.workout.repository;

import com.fitnessapp.workout.model.Workout;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkoutRepository extends CrudRepository<Workout, UUID> {


}
