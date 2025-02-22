package com.fitnessapp.web;

import com.fitnessapp.user.service.UserService;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.service.WorkoutService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;
    private final UserService userService;

    public WorkoutController(WorkoutService workoutService,
                             UserService userService) {
        this.workoutService = workoutService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getWorkoutPage() {

        List<Workout> allWorkouts = workoutService.getAllWorkouts();

        ModelAndView modelAndView = new ModelAndView("workouts");
        modelAndView.addObject("workouts", allWorkouts);

        return modelAndView;
    }

}
