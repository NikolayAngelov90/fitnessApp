package com.fitnessapp.web;

import com.fitnessapp.reports.client.dto.MembershipReport;
import com.fitnessapp.reports.client.dto.WorkoutGeneralReport;
import com.fitnessapp.reports.client.dto.WorkoutTrainerReport;
import com.fitnessapp.reports.client.dto.WorkoutTypeReport;
import com.fitnessapp.reports.service.ReportService;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.workout.model.WorkoutType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    public ReportController(ReportService reportService,
                            UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    @GetMapping("/workouts")
    public ModelAndView getWorkoutReportPage() {

        List<User> trainers = userService.getAllApprovedTrainers();

        ModelAndView modelAndView = new ModelAndView("workouts-reports");
        modelAndView.addObject("trainers", trainers);
        modelAndView.addObject("workoutsType", WorkoutType.values());

        return modelAndView;
    }

    @GetMapping("/memberships")
    public String getMembershipReportPage() {

        return "memberships-reports";
    }

    @GetMapping("/workouts/by-type")
    public String generateWorkoutTypeReport(@RequestParam WorkoutType workoutType,
                                            @RequestParam YearMonth month,
                                            RedirectAttributes redirectAttributes) {

        WorkoutTypeReport workoutTypeReport = reportService.getWorkoutTypeReport(workoutType, month.atDay(1));
        redirectAttributes.addFlashAttribute("workoutTypeReport", workoutTypeReport);

        return "redirect:/reports/workouts";
    }

    @GetMapping("/workouts/by-trainer")
    public String generateWorkoutTrainerReport(@RequestParam UUID trainerId,
                                               @RequestParam YearMonth month,
                                               RedirectAttributes redirectAttributes) {

        User trainer = userService.getById(trainerId);
        redirectAttributes.addFlashAttribute("trainer", trainer);

        WorkoutTrainerReport workoutTrainerReport = reportService.getWorkoutTrainerReport(trainerId, month.atDay(1));
        redirectAttributes.addFlashAttribute("workoutTrainerReport", workoutTrainerReport);

        return "redirect:/reports/workouts";
    }

    @GetMapping("/workouts/general")
    public String generateWorkoutGeneralReport(@RequestParam YearMonth fromMonth,
                                               @RequestParam YearMonth toMonth,
                                               RedirectAttributes redirectAttributes) {

        WorkoutGeneralReport workoutGeneralReport = reportService.getWorkoutGeneralReport(fromMonth.atDay(1), toMonth.atDay(1));
        redirectAttributes.addFlashAttribute("workoutGeneralReport", workoutGeneralReport);

        return "redirect:/reports/workouts";
    }

    @GetMapping("/memberships/monthly")
    public String generateMembershipReport(@RequestParam YearMonth month,
                                           RedirectAttributes redirectAttributes) {

        MembershipReport membershipReport = reportService.getMembershipReport(month.atDay(1));
        redirectAttributes.addFlashAttribute("membershipReport", membershipReport);

        return "redirect:/reports/memberships";
    }
}
