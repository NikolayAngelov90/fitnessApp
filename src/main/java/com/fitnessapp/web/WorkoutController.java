package com.fitnessapp.web;

import com.fitnessapp.payment.service.PaymentService;
import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.web.dto.WorkoutRequest;
import com.fitnessapp.web.mapper.DtoMapper;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
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

        LocalDate today = LocalDate.now();

        List<Workout> allDisplayedWorkouts = workoutService.getAllDisplayedWorkouts(today);

        ModelAndView modelAndView = new ModelAndView("workouts");
        modelAndView.addObject("workouts", allDisplayedWorkouts);

        return modelAndView;
    }

    @GetMapping("/{id}/book")
    @PreAuthorize("hasRole('CLIENT')")
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
    @PreAuthorize("hasRole('CLIENT')")
    public ModelAndView showRegisteredClientWorkouts(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        List<Workout> allRegisteredClientWorkouts = workoutService
                .getAllRegisteredClientWorkouts(customUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView("client-workouts");
        modelAndView.addObject("workouts", allRegisteredClientWorkouts);

        return modelAndView;
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    public String cancelBooking(@PathVariable UUID id,
                                @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        workoutService.cancelBookingWorkout(id, customUserDetails.getUserId());

        return "redirect:/workouts/client-registered";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('TRAINER')")
    public ModelAndView getCreateWorkoutPage() {

        ModelAndView modelAndView = new ModelAndView("create-workout");
        modelAndView.addObject("workoutRequest", WorkoutRequest.empty());

        return modelAndView;
    }

    @PostMapping("/create")
    public String createNewWorkout(@Valid WorkoutRequest workoutRequest,
                                   BindingResult bindingResult,
                                   @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                   RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "create-workout";
        }

        workoutService.create(workoutRequest, customUserDetails.getUserId());

        redirectAttributes.addFlashAttribute("NewWorkoutMessage", "Successfully created new workout");

        return "redirect:/home";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('TRAINER')")
    public ModelAndView getEditWorkoutPage(@PathVariable UUID id) {

        Workout workout = workoutService.getById(id);

        ModelAndView modelAndView = new ModelAndView("edit-workout");
        modelAndView.addObject("workout", workout);
        modelAndView.addObject("workoutRequest", DtoMapper.mapWorkoutToWorkoutRequest(workout));

        return modelAndView;
    }

    @PutMapping("/{id}/edit")
    public ModelAndView update(@PathVariable UUID id,
                               @Valid WorkoutRequest workoutRequest,
                               BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("edit-workout");
        }

        Workout workout = workoutService.getById(id);
        workoutService.edit(workout, workoutRequest);

        ModelAndView modelAndView = new ModelAndView("edit-workout");
        modelAndView.addObject("workout", workout);
        modelAndView.addObject("message", "Successfully updated workout");

        return modelAndView;
    }

    @PutMapping("/{id}/delete")
    public String softDelete(@PathVariable UUID id) {

        workoutService.changeStatusDeleted(id);

        return "redirect:/home";
    }
}
