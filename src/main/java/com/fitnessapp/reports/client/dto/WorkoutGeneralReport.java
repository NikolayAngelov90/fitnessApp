package com.fitnessapp.reports.client.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkoutGeneralReport {

    private LocalDate fromMonth;

    private LocalDate toMonth;

    private int totalWorkouts;

    private double averageDuration;

    private int totalParticipants;
}
