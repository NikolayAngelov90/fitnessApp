package com.fitnessapp.web;

import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.subscription.service.SubscriptionService;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.model.UserRole;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.web.dto.RegisterRequest;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class IndexController {

    private final UserService userService;
    private final WorkoutService workoutService;
    private final SubscriptionService subscriptionService;

    public IndexController(UserService userService,
                           WorkoutService workoutService,
                           SubscriptionService subscriptionService) {
        this.userService = userService;
        this.workoutService = workoutService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/")
    public String getIndexPage(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        if (customUserDetails != null) {
            if (customUserDetails.getRole() == UserRole.CLIENT) {
                return "redirect:/home-client";
            } else if (customUserDetails.getRole() == UserRole.TRAINER) {
                return "redirect:/home-trainer";
            } else if (customUserDetails.getRole() == UserRole.ADMIN) {
                return "redirect:/home-admin";
            }
        }

        return "index";
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        if (customUserDetails != null) {
            if (customUserDetails.getRole() == UserRole.CLIENT) {
                return new ModelAndView("redirect:/home-client");
            } else if (customUserDetails.getRole() == UserRole.TRAINER) {
                return new ModelAndView("redirect:/home-trainer");
            } else if (customUserDetails.getRole() == UserRole.ADMIN) {
                return new ModelAndView("redirect:/home-admin");
            }
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("registerRequest", RegisterRequest.empty());

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerNewUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("register");
        }

        userService.register(registerRequest);

        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/login")
    public String Login(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        if (customUserDetails != null) {
            if (customUserDetails.getRole() == UserRole.CLIENT) {
                return "redirect:/home-client";
            } else if (customUserDetails.getRole() == UserRole.TRAINER) {
                return "redirect:/home-trainer";
            } else if (customUserDetails.getRole() == UserRole.ADMIN) {
                return "redirect:/home-admin";
            }
        }

        return "login";
    }

    @GetMapping("/home-client")
    @PreAuthorize("hasRole('CLIENT')")
    public ModelAndView getClientHomePage(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        User user = userService.getById(customUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);

        int allCompletedMonthWorkouts = workoutService.getAllCompletedMonthWorkouts();
        int monthCompletedWorkoutsForClientCount = workoutService.getAllCompletedMonthWorkoutsForClient(user);
        double totalPercentage = (double) monthCompletedWorkoutsForClientCount / allCompletedMonthWorkouts * 100;

        modelAndView.setViewName("home-client");
        modelAndView.addObject("monthCompletedWorkoutsForClientCount", monthCompletedWorkoutsForClientCount);
        modelAndView.addObject("allCompletedMonthWorkouts", allCompletedMonthWorkouts);
        modelAndView.addObject("totalPercentage", totalPercentage);

        return modelAndView;
    }

    @GetMapping("/home-trainer")
    @PreAuthorize("hasRole('TRAINER')")
    public ModelAndView getTrainerHomePage(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        User user = userService.getById(customUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);

        List<Workout> upcomingWorkoutsByTrainer = workoutService.getUpcomingWorkoutsByTrainer(user);
        int monthCompletedWorkoutsCount = workoutService
                .getMonthCompletedWorkoutsByTrainer(user)
                .size();
        double monthlyAttendancePercentage = workoutService.calculateMonthlyAttendancePercentage(user);

        modelAndView.setViewName("home-trainer");
        modelAndView.addObject("upcomingWorkouts", upcomingWorkoutsByTrainer);
        modelAndView.addObject("monthCompletedWorkoutsCount", monthCompletedWorkoutsCount);
        modelAndView.addObject("monthlyAttendancePercentage", monthlyAttendancePercentage);

        return modelAndView;
    }

    @GetMapping("/home-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getAdminHomePage() {

        List<User> pendingApproveTrainers = userService.getPendingApproveTrainers();

        List<Subscription> subscriptions = subscriptionService.getAll();

        ModelAndView modelAndView = new ModelAndView("home-admin");
        modelAndView.addObject("pendingApproveTrainers", pendingApproveTrainers);
        modelAndView.addObject("subscriptions", subscriptions);

        return modelAndView;
    }
}

