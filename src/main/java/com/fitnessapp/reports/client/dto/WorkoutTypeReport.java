package com.fitnessapp.reports.client.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkoutTypeReport {

    private String type;

    private LocalDate month;

    private int totalWorkouts;

    private double averageDuration;

    private int totalParticipants;
}
