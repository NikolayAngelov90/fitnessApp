package com.fitnessapp.web;

import com.fitnessapp.payment.service.PaymentService;
import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.model.UserRole;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.web.dto.WorkoutRequest;
import com.fitnessapp.workout.model.RecurringType;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.model.WorkoutType;
import com.fitnessapp.workout.service.WorkoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WorkoutControllerUTest {

    private WorkoutService workoutService;
    private PaymentService paymentService;
    private UserService userService;
    private WorkoutController controller;

    @BeforeEach
    void setUp() {
        workoutService = mock(WorkoutService.class);
        paymentService = mock(PaymentService.class);
        userService = mock(UserService.class);
        controller = new WorkoutController(workoutService, paymentService, userService);
    }

    @Test
    void getWorkoutPage_returnsWorkoutsAndTrainers() {
        List<Workout> workouts = List.of(Workout.builder().duration(30).build());
        when(workoutService.getAllDisplayedWorkouts(any(LocalDate.class), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(workouts);
        when(userService.getAllApprovedTrainers()).thenReturn(List.of(User.builder().email("t@e.com").build()));

        ModelAndView mv = controller.getWorkoutPage(null, null, null, null);
        assertEquals("workouts", mv.getViewName());
        assertEquals(workouts, mv.getModel().get("workouts"));
        assertNotNull(mv.getModel().get("trainers"));
    }

    @Test
    void showWorkoutPaymentPage_returnsWorkoutPaymentView() {
        UUID id = UUID.randomUUID();
        Workout w = Workout.builder().duration(45).build();
        when(workoutService.getById(id)).thenReturn(w);

        ModelAndView mv = controller.showWorkoutPaymentPage(id);
        assertEquals("workout-payment", mv.getViewName());
        assertEquals(w, mv.getModel().get("workout"));
    }

    @Test
    void processBook_callsPayment_andReturnsMessage() {
        UUID id = UUID.randomUUID();
        UUID uid = UUID.randomUUID();
        Workout w = Workout.builder().duration(30).build();
        when(workoutService.getById(id)).thenReturn(w);
        CustomUserDetails cud = new CustomUserDetails(uid, "e@e.com", "pwd", UserRole.CLIENT, false);

        ModelAndView mv = controller.processBook(id, cud);
        assertEquals("workout-payment", mv.getViewName());
        assertEquals(w, mv.getModel().get("workout"));
        assertEquals("Payment Successful", mv.getModel().get("message"));
        verify(paymentService).processWorkoutPayment(w, uid);
    }

    @Test
    void showRegisteredClientWorkouts_returnsClientWorkouts() {
        UUID uid = UUID.randomUUID();
        CustomUserDetails cud = new CustomUserDetails(uid, "e@e.com", "pwd", UserRole.CLIENT, false);
        when(workoutService.getAllRegisteredClientWorkouts(uid)).thenReturn(List.of(Workout.builder().duration(20).build()));

        ModelAndView mv = controller.showRegisteredClientWorkouts(cud);
        assertEquals("client-workouts", mv.getViewName());
        assertNotNull(mv.getModel().get("workouts"));
    }

    @Test
    void cancelBooking_callsService_andRedirects() {
        UUID id = UUID.randomUUID();
        UUID uid = UUID.randomUUID();
        CustomUserDetails cud = new CustomUserDetails(uid, "e@e.com", "pwd", UserRole.CLIENT, false);

        String view = controller.cancelBooking(id, cud);
        assertEquals("redirect:/workouts/client-registered", view);
        verify(workoutService).cancelBookingWorkout(id, uid);
    }

    @Test
    void getCreateWorkoutPage_returnsCreateWorkoutWithEmptyForm() {
        ModelAndView mv = controller.getCreateWorkoutPage();
        assertEquals("create-workout", mv.getViewName());
        assertNotNull(mv.getModel().get("workoutRequest"));
    }

    @Test
    void createNewWorkout_withErrors_returnsCreateWorkout() {
        WorkoutRequest req = new WorkoutRequest(null, null, null, null, null, "", null);
        BindingResult errors = new BeanPropertyBindingResult(req, "workoutRequest");
        errors.rejectValue("workoutType", "invalid", "Invalid");
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        CustomUserDetails cud = new CustomUserDetails(UUID.randomUUID(), "t@e.com", "pwd", UserRole.TRAINER, true);

        String view = controller.createNewWorkout(req, errors, cud, attrs);
        assertEquals("create-workout", view);
        verifyNoInteractions(workoutService);
    }

    @Test
    void createNewWorkout_success_redirectsWithFlash_andCallsService() {
        WorkoutRequest req = new WorkoutRequest(
                WorkoutType.YOGA,
                30,
                BigDecimal.valueOf(10),
                LocalDateTime.now().plusDays(1),
                RecurringType.NONE,
                "A valid description",
                10
        );
        BindingResult errors = new BeanPropertyBindingResult(req, "workoutRequest");
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        UUID uid = UUID.randomUUID();
        CustomUserDetails cud = new CustomUserDetails(uid, "t@e.com", "pwd", UserRole.TRAINER, true);

        String view = controller.createNewWorkout(req, errors, cud, attrs);
        assertEquals("redirect:/home-trainer", view);
        assertEquals("Successfully created new workout", attrs.getFlashAttributes().get("NewWorkoutMessage"));
        verify(workoutService).create(req, uid);
    }

    @Test
    void getEditWorkoutPage_returnsEditViewWithWorkoutAndRequest() {
        UUID id = UUID.randomUUID();
        Workout w = Workout.builder().duration(25).build();
        when(workoutService.getById(id)).thenReturn(w);

        ModelAndView mv = controller.getEditWorkoutPage(id);
        assertEquals("edit-workout", mv.getViewName());
        assertEquals(w, mv.getModel().get("workout"));
        assertNotNull(mv.getModel().get("workoutRequest"));
    }

    @Test
    void update_withErrors_returnsEditWorkout() {
        WorkoutRequest req = new WorkoutRequest(null, null, null, null, null, "", null);
        BindingResult errors = new BeanPropertyBindingResult(req, "workoutRequest");
        errors.rejectValue("duration", "invalid", "Invalid");

        ModelAndView mv = controller.update(UUID.randomUUID(), req, errors);
        assertEquals("edit-workout", mv.getViewName());
        verifyNoInteractions(workoutService);
    }

    @Test
    void update_success_editsAndReturnsMessage() {
        UUID id = UUID.randomUUID();
        Workout w = Workout.builder().duration(20).build();
        when(workoutService.getById(id)).thenReturn(w);

        WorkoutRequest req = new WorkoutRequest(
                WorkoutType.SPINNING,
                30,
                BigDecimal.valueOf(15),
                LocalDateTime.now().plusDays(1),
                RecurringType.NONE,
                "Some description",
                12
        );
        BindingResult errors = new BeanPropertyBindingResult(req, "workoutRequest");

        ModelAndView mv = controller.update(id, req, errors);
        assertEquals("edit-workout", mv.getViewName());
        assertEquals(w, mv.getModel().get("workout"));
        assertEquals("Successfully updated workout", mv.getModel().get("message"));
        verify(workoutService).edit(w, req);
    }

    @Test
    void softDelete_callsService_andRedirects() {
        UUID id = UUID.randomUUID();
        String view = controller.softDelete(id);
        assertEquals("redirect:/home-trainer", view);
        verify(workoutService).changeStatusDeleted(id);
    }

    @Test
    void showTrainerWorkouts_returnsTrainerWorkouts() {
        UUID uid = UUID.randomUUID();
        CustomUserDetails cud = new CustomUserDetails(uid, "t@e.com", "pwd", UserRole.TRAINER, true);
        when(workoutService.getAllWorkoutsByTrainer(uid)).thenReturn(List.of(Workout.builder().duration(35).build()));

        ModelAndView mv = controller.showTrainerWorkouts(cud);
        assertEquals("trainer-workouts", mv.getViewName());
        assertNotNull(mv.getModel().get("trainerWorkouts"));
    }

    @Test
    void showParticipantsWorkout_returnsWorkoutParticipantsView() {
        UUID id = UUID.randomUUID();
        Workout w = Workout.builder().duration(40).build();
        when(workoutService.getById(id)).thenReturn(w);

        ModelAndView mv = controller.showParticipantsWorkout(id);
        assertEquals("workout-participants", mv.getViewName());
        assertEquals(w, mv.getModel().get("workout"));
    }
}
