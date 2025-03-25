package com.fitnessapp.reports.client;

import com.fitnessapp.reports.client.config.CustomErrorDecoder;
import com.fitnessapp.reports.client.dto.MembershipReport;
import com.fitnessapp.reports.client.dto.WorkoutGeneralReport;
import com.fitnessapp.reports.client.dto.WorkoutTrainerReport;
import com.fitnessapp.reports.client.dto.WorkoutTypeReport;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.UUID;

@FeignClient(name = "fitness-reports-svc", url = "${fitness-reports-svc.base-url}", configuration = CustomErrorDecoder.class)
public interface ReportClient {

    @GetMapping("/workout-reports/by-type")
    ResponseEntity<WorkoutTypeReport> getTypeReport(@RequestParam String workoutType,
                                                    @RequestParam LocalDate month);

    @GetMapping("/workout-reports/by-trainer")
    ResponseEntity<WorkoutTrainerReport> getTrainerReport(@RequestParam UUID trainerId,
                                                          @RequestParam LocalDate month);

    @GetMapping("/workout-reports/general")
    ResponseEntity<WorkoutGeneralReport> getWorkoutGeneralReport(@RequestParam LocalDate fromMonth,
                                                                 @RequestParam LocalDate toMonth);

    @GetMapping("/membership-reports/monthly")
    ResponseEntity<MembershipReport> getMembershipReport(@RequestParam LocalDate month);
}
