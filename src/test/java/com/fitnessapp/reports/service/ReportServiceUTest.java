package com.fitnessapp.reports.service;

import com.fitnessapp.reports.client.ReportClient;
import com.fitnessapp.reports.client.dto.MembershipReport;
import com.fitnessapp.reports.client.dto.WorkoutGeneralReport;
import com.fitnessapp.reports.client.dto.WorkoutTrainerReport;
import com.fitnessapp.reports.client.dto.WorkoutTypeReport;
import com.fitnessapp.workout.model.WorkoutType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReportServiceUTest {

    private ReportClient reportClient;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportClient = mock(ReportClient.class);
        reportService = new ReportService(reportClient);
    }

    @Test
    void getWorkoutTypeReport_returnsBody_onSuccess() {
        WorkoutTypeReport report = new WorkoutTypeReport();
        when(reportClient.getTypeReport(eq("YOGA"), any())).thenReturn(ResponseEntity.ok(report));

        WorkoutTypeReport result = reportService.getWorkoutTypeReport(WorkoutType.YOGA, LocalDate.now());
        assertEquals(report, result);
    }

    @Test
    void getWorkoutTypeReport_returnsNull_onServerError() {
        when(reportClient.getTypeReport(eq("YOGA"), any())).thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
        assertNull(reportService.getWorkoutTypeReport(WorkoutType.YOGA, LocalDate.now()));
    }

    @Test
    void getWorkoutTrainerReport_returnsBody_onSuccess() {
        WorkoutTrainerReport report = new WorkoutTrainerReport();
        when(reportClient.getTrainerReport(any(), any())).thenReturn(ResponseEntity.ok(report));

        WorkoutTrainerReport result = reportService.getWorkoutTrainerReport(UUID.randomUUID(), LocalDate.now());
        assertEquals(report, result);
    }

    @Test
    void getWorkoutTrainerReport_returnsNull_onServerError() {
        when(reportClient.getTrainerReport(any(), any())).thenReturn(ResponseEntity.status(500).body(null));
        assertNull(reportService.getWorkoutTrainerReport(UUID.randomUUID(), LocalDate.now()));
    }

    @Test
    void getWorkoutGeneralReport_returnsBody_onSuccess() {
        WorkoutGeneralReport report = new WorkoutGeneralReport();
        when(reportClient.getWorkoutGeneralReport(any(), any())).thenReturn(ResponseEntity.ok(report));

        WorkoutGeneralReport result = reportService.getWorkoutGeneralReport(LocalDate.now().minusMonths(1), LocalDate.now());
        assertEquals(report, result);
    }

    @Test
    void getWorkoutGeneralReport_returnsNull_onServerError() {
        when(reportClient.getWorkoutGeneralReport(any(), any())).thenReturn(ResponseEntity.status(500).body(null));
        assertNull(reportService.getWorkoutGeneralReport(LocalDate.now().minusMonths(1), LocalDate.now()));
    }

    @Test
    void getMembershipReport_returnsBody_onSuccess() {
        MembershipReport report = new MembershipReport();
        when(reportClient.getMembershipReport(any())).thenReturn(ResponseEntity.ok(report));

        MembershipReport result = reportService.getMembershipReport(LocalDate.now());
        assertEquals(report, result);
    }

    @Test
    void getMembershipReport_returnsNull_onServerError() {
        when(reportClient.getMembershipReport(any())).thenReturn(ResponseEntity.status(500).body(null));
        assertNull(reportService.getMembershipReport(LocalDate.now()));
    }
}
