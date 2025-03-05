package com.fitnessapp.web;

import com.fitnessapp.payment.service.PaymentService;
import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.service.WorkoutService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;
    private final UserService userService;
    private final PaymentService paymentService;

    public WorkoutController(WorkoutService workoutService,
                             UserService userService,
                             PaymentService paymentService) {
        this.workoutService = workoutService;
        this.userService = userService;
        this.paymentService = paymentService;
    }

    @GetMapping
    public ModelAndView getWorkoutPage() {

        List<Workout> allWorkouts = workoutService.getAllWorkouts();

        ModelAndView modelAndView = new ModelAndView("workouts");
        modelAndView.addObject("workouts", allWorkouts);

        return modelAndView;
    }

    @GetMapping("/{id}/book")
    public ModelAndView showWorkoutPaymentPage(@PathVariable UUID id) {

        Workout workout = workoutService.getById(id);

        ModelAndView modelAndView = new ModelAndView("workout-payment");
        modelAndView.addObject("workout", workout);

        return modelAndView;
    }

    @PostMapping("/{id}/book")
    public ModelAndView processBook(@PathVariable UUID id,
                                    @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Workout workout = workoutService.getById(id);

        ModelAndView modelAndView = new ModelAndView("workout-payment");
        modelAndView.addObject("workout", workout);

        User user = userService.getById(customUserDetails.getUserId());
        paymentService.processWorkoutPayment(workout, user);

        modelAndView.addObject("message", "Payment Successful");

        return modelAndView;
    }
}
