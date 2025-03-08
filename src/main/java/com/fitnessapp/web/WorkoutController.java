package com.fitnessapp.web;

import com.fitnessapp.payment.service.PaymentService;
import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.service.WorkoutService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;
    private final PaymentService paymentService;

    public WorkoutController(WorkoutService workoutService,
                             PaymentService paymentService) {
        this.workoutService = workoutService;
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

        paymentService.processWorkoutPayment(workout, customUserDetails.getUserId());

        modelAndView.addObject("message", "Payment Successful");

        return modelAndView;
    }

    @GetMapping("/client-registered")
    public ModelAndView showRegisteredClientWorkouts(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        List<Workout> allRegisteredClientWorkouts = workoutService
                .getAllRegisteredClientWorkouts(customUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView("client-workouts");
        modelAndView.addObject("workouts", allRegisteredClientWorkouts);

        return modelAndView;
    }

    @PutMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable UUID id,
                                @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        workoutService.cancelBookingWorkout(id, customUserDetails.getUserId());

        return "redirect:/workouts/client-registered";
    }
}
