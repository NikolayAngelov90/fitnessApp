package com.fitnessapp.reports.client.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class WorkoutTrainerReport {

    private UUID trainerId;

    private LocalDate month;

    private int totalWorkouts;

    private double averageDuration;

    private int totalParticipants;
}
