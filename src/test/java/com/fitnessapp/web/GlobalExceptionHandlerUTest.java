package com.fitnessapp.web;

import com.fitnessapp.exception.*;
import com.fitnessapp.payment.model.PaymentProductType;
import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.model.UserRole;
import com.fitnessapp.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GlobalExceptionHandlerUTest {

    private UserService userService;
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);
        handler = new GlobalExceptionHandler(userService);
    }

    @Test
    void handleUserAlreadyExistsException_shouldRedirectToRegisterWithFlashError() {
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        String view = handler.handleUserAlreadyExistsException(new UserAlreadyExistsException("Email exists"), attrs);

        assertEquals("redirect:/register", view);
        assertEquals("Email exists", attrs.getFlashAttributes().get("error"));
    }

    @Test
    void handleImageUploadException_shouldRedirectByRoleAndSetFlashError() {
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        CustomUserDetails cud = new CustomUserDetails(UUID.randomUUID(), "e@e.com", "pwd", UserRole.CLIENT, false);

        String view = handler.handleImageUploadException(new ImageUploadException("Bad image"), cud, attrs);

        assertEquals("redirect:/home-client", view);
        assertEquals("Bad image", attrs.getFlashAttributes().get("error"));
    }

    @Test
    void handlePhoneNumberAlreadyExistsException_shouldRedirectToUserEdit() {
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        String view = handler.handlePhoneNumberAlreadyExistsException(new PhoneNumberAlreadyExistsException("exists"), attrs);
        assertEquals("redirect:/users/edit", view);
        assertEquals("exists", attrs.getFlashAttributes().get("error"));
    }

    @Test
    void handlePaymentFailedException_subscription_shouldRedirectToMembershipPayment() {
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        String view = handler.handlePaymentFailedException(new PaymentFailedException("fail", PaymentProductType.SUBSCRIPTION), attrs);
        assertEquals("redirect:/memberships/{id}/payment", view);
        assertEquals("fail", attrs.getFlashAttributes().get("error"));
    }

    @Test
    void handlePaymentFailedException_workout_shouldRedirectToWorkoutBook() {
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        String view = handler.handlePaymentFailedException(new PaymentFailedException("fail", PaymentProductType.WORKOUT), attrs);
        assertEquals("redirect:/workouts/{id}/book", view);
        assertEquals("fail", attrs.getFlashAttributes().get("error"));
    }

    @Test
    void handleBusinessException_shouldRedirectToHomeClientWithFlash() {
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        String view = handler.handleBusinessException(new BusinessException("stop it"), attrs);
        assertEquals("redirect:/home-client", view);
        assertEquals("stop it", attrs.getFlashAttributes().get("stopMembershipError"));
    }

    @Test
    void handlePaymentNotFoundException_shouldReturnPaymentHistoryViewWithUserAndMessage() {
        UUID userId = UUID.randomUUID();
        CustomUserDetails cud = new CustomUserDetails(userId, "e@e.com", "pwd", UserRole.CLIENT, false);
        User u = User.builder().id(userId).email("e@e.com").build();
        when(userService.getById(userId)).thenReturn(u);

        ModelAndView mv = handler.handlePaymentNotFoundException(new PaymentNotFoundException("not found"), cud);

        assertEquals("payment-history", mv.getViewName());
        assertEquals(u, mv.getModel().get("user"));
        assertEquals("not found", mv.getModel().get("message"));
    }

    @Test
    void handleTrainerNotApproveException_shouldRedirectHomeTrainer() {
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        String view = handler.handleTrainerNotApproveException(new TrainerNotApproveException("no"), attrs);
        assertEquals("redirect:/home-trainer", view);
        assertEquals("no", attrs.getFlashAttributes().get("noPermissionError"));
    }

    @Test
    void handleWorkoutReportNotFoundException_shouldRedirectReportsWorkouts() {
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        String view = handler.handleWorkoutReportNotFoundException(new WorkoutReportNotFoundException("none"), attrs);
        assertEquals("redirect:/reports/workouts", view);
        assertEquals("none", attrs.getFlashAttributes().get("notFoundError"));
    }

    @Test
    void handleMembershipReportNotFoundException_shouldRedirectReportsMemberships() {
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        String view = handler.handleMembershipReportNotFoundException(new MembershipReportNotFoundException("none"), attrs);
        assertEquals("redirect:/reports/memberships", view);
        assertEquals("none", attrs.getFlashAttributes().get("notFoundError"));
    }

    @Test
    void handleNotFoundExceptions_shouldReturnNotFoundView() {
        ModelAndView mv = handler.handleNotFoundExceptions();
        assertEquals("not-found", mv.getViewName());
    }

    @Test
    void handleAnyException_shouldReturnInternalServerErrorViewWithErrorName() {
        ModelAndView mv = handler.handleAnyException(new IllegalStateException("boom"));
        assertEquals("internal-server-error", mv.getViewName());
        assertEquals("IllegalStateException", mv.getModel().get("errorMessage"));
    }
}
