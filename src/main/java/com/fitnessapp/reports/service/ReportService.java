package com.fitnessapp.reports.service;

import com.fitnessapp.reports.client.ReportClient;
import com.fitnessapp.reports.client.dto.MembershipReport;
import com.fitnessapp.reports.client.dto.WorkoutGeneralReport;
import com.fitnessapp.reports.client.dto.WorkoutTrainerReport;
import com.fitnessapp.reports.client.dto.WorkoutTypeReport;
import com.fitnessapp.workout.model.WorkoutType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
public class ReportService {

    private final ReportClient reportClient;

    public ReportService(ReportClient reportClient) {
        this.reportClient = reportClient;
    }


    public WorkoutTypeReport getWorkoutTypeReport(WorkoutType workoutType, LocalDate month) {

        ResponseEntity<WorkoutTypeReport> httpResponse = reportClient.getTypeReport(workoutType.toString(), month);

        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error getting workout type report");
        }

        return httpResponse.getBody();
    }

    public WorkoutTrainerReport getWorkoutTrainerReport(UUID trainerId, LocalDate month) {

        ResponseEntity<WorkoutTrainerReport> httpResponse = reportClient.getTrainerReport(trainerId, month);

        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error getting workout trainer report");
        }

        return httpResponse.getBody();
    }

    public WorkoutGeneralReport getWorkoutGeneralReport(LocalDate fromMonth, LocalDate toMonth) {

        ResponseEntity<WorkoutGeneralReport> httpResponse = reportClient.getWorkoutGeneralReport(fromMonth, toMonth);

        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error getting workout general report");
        }

        return httpResponse.getBody();
    }

    public MembershipReport getMembershipReport(LocalDate month) {

        ResponseEntity<MembershipReport> httpResponse = reportClient.getMembershipReport(month);

        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error getting membership report");
        }

        return httpResponse.getBody();
    }
}
