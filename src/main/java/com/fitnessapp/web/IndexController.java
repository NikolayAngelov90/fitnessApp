package com.fitnessapp.web;

import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.model.UserRole;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.web.dto.RegisterRequest;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.List;

@Controller
public class IndexController {

    private final UserService userService;
    private final WorkoutService workoutService;

    public IndexController(UserService userService,
                           WorkoutService workoutService) {
        this.userService = userService;
        this.workoutService = workoutService;
    }

    @GetMapping("/")
    public String getIndexPage(Principal principal) {

        if (principal != null) {
            return "redirect:/home";
        }

        return "index";
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage(Principal principal) {

        if (principal != null) {
            return new ModelAndView("redirect:/home");
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
    public String Login(Principal principal) {

        if (principal != null) {
            return "redirect:/home";
        }

        return "login";
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        User user = userService.getById(customUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);

        if (user.getRole() == UserRole.CLIENT) {
            modelAndView = new ModelAndView("home-client");
        } else if (user.getRole() == UserRole.TRAINER) {
            modelAndView = new ModelAndView("home-trainer");
            List<Workout> trainerWorkouts = workoutService.getUpcomingWorkoutsByTrainer(user);
            modelAndView.addObject("trainerWorkouts", trainerWorkouts);
        } else if (user.getRole() == UserRole.ADMIN) {
            modelAndView = new ModelAndView("home-admin");
        }

        return modelAndView;
    }
}
