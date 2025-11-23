package com.fitnessapp.web;

import com.fitnessapp.reports.client.dto.MembershipReport;
import com.fitnessapp.reports.client.dto.WorkoutGeneralReport;
import com.fitnessapp.reports.client.dto.WorkoutTrainerReport;
import com.fitnessapp.reports.client.dto.WorkoutTypeReport;
import com.fitnessapp.reports.service.ReportService;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.workout.model.WorkoutType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReportControllerUTest {

    private ReportService reportService;
    private UserService userService;
    private ReportController controller;

    @BeforeEach
    void setUp() {
        reportService = mock(ReportService.class);
        userService = mock(UserService.class);
        controller = new ReportController(reportService, userService);
    }

    @Test
    void getWorkoutReportPage_returnsViewWithTrainersAndTypes() {
        when(userService.getAllApprovedTrainers()).thenReturn(List.of(User.builder().email("t@e.com").build()));

        ModelAndView mv = controller.getWorkoutReportPage();
        assertEquals("workouts-reports", mv.getViewName());
        assertNotNull(mv.getModel().get("trainers"));
        assertNotNull(mv.getModel().get("workoutsType"));
        assertTrue(((Object[]) mv.getModel().get("workoutsType")).length > 0);
    }

    @Test
    void getMembershipReportPage_returnsViewName() {
        String view = controller.getMembershipReportPage();
        assertEquals("memberships-reports", view);
    }

    @Test
    void generateWorkoutTypeReport_addsFlashAndRedirects() {
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        WorkoutTypeReport report = new WorkoutTypeReport();
        when(reportService.getWorkoutTypeReport(eq(WorkoutType.YOGA), any())).thenReturn(report);

        String view = controller.generateWorkoutTypeReport(WorkoutType.YOGA, YearMonth.now(), attrs);
        assertEquals("redirect:/reports/workouts", view);
        assertEquals(report, attrs.getFlashAttributes().get("workoutTypeReport"));
    }

    @Test
    void generateWorkoutTrainerReport_addsTrainerAndReportAndRedirects() {
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        UUID trainerId = UUID.randomUUID();
        User trainer = User.builder().id(trainerId).email("t@e.com").build();
        when(userService.getById(trainerId)).thenReturn(trainer);
        WorkoutTrainerReport report = new WorkoutTrainerReport();
        when(reportService.getWorkoutTrainerReport(eq(trainerId), any())).thenReturn(report);

        String view = controller.generateWorkoutTrainerReport(trainerId, YearMonth.now(), attrs);
        assertEquals("redirect:/reports/workouts", view);
        assertEquals(trainer, attrs.getFlashAttributes().get("trainer"));
        assertEquals(report, attrs.getFlashAttributes().get("workoutTrainerReport"));
    }

    @Test
    void generateWorkoutGeneralReport_addsFlashAndRedirects() {
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        WorkoutGeneralReport report = new WorkoutGeneralReport();
        when(reportService.getWorkoutGeneralReport(any(), any())).thenReturn(report);

        String view = controller.generateWorkoutGeneralReport(YearMonth.now().minusMonths(1), YearMonth.now(), attrs);
        assertEquals("redirect:/reports/workouts", view);
        assertEquals(report, attrs.getFlashAttributes().get("workoutGeneralReport"));
    }

    @Test
    void generateMembershipReport_addsFlashAndRedirects() {
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        MembershipReport report = new MembershipReport();
        when(reportService.getMembershipReport(any())).thenReturn(report);

        String view = controller.generateMembershipReport(YearMonth.now(), attrs);
        assertEquals("redirect:/reports/memberships", view);
        assertEquals(report, attrs.getFlashAttributes().get("membershipReport"));
    }
}
