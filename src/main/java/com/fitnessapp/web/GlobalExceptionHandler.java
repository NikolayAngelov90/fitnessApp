package com.fitnessapp.web;

import com.fitnessapp.exception.*;
import com.fitnessapp.payment.model.PaymentProductType;
import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;

import static com.fitnessapp.security.CustomAuthenticationSuccessHandler.validateRedirectEndpoint;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final UserService userService;

    public GlobalExceptionHandler(UserService userService) {
        this.userService = userService;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public String handleUserAlreadyExistsException(UserAlreadyExistsException e,
                                                   RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("error", e.getMessage());

        return "redirect:/register";
    }

    @ExceptionHandler(ImageUploadException.class)
    public String handleImageUploadException(ImageUploadException e,
                                             @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                             RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("error", e.getMessage());

        return validateRedirectEndpoint(customUserDetails);
    }

    @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
    public String handlePhoneNumberAlreadyExistsException(PhoneNumberAlreadyExistsException e,
                                                          RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("error", e.getMessage());

        return "redirect:/users/edit";
    }

    @ExceptionHandler(PaymentFailedException.class)
    public String handlePaymentFailedException(PaymentFailedException e,
                                               RedirectAttributes redirectAttributes) {

        String redirectPath = "";
        if (e.getProductType() == PaymentProductType.SUBSCRIPTION) {
            redirectPath = "redirect:/memberships/{id}/payment";
        } else if (e.getProductType() == PaymentProductType.WORKOUT) {
            redirectPath = "redirect:/workouts/{id}/book";
        }

        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return redirectPath;
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException e,
                                          RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("stopMembershipError", e.getMessage());

        return "redirect:/home-client";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(PaymentNotFoundException.class)
    public ModelAndView handlePaymentNotFoundException(PaymentNotFoundException e,
                                                       @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        ModelAndView modelAndView = new ModelAndView("payment-history");
        modelAndView.addObject("user", userService.getById(customUserDetails.getUserId()));
        modelAndView.addObject("message", e.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(TrainerNotApproveException.class)
    public String handleTrainerNotApproveException(TrainerNotApproveException e,
                                                   RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("noPermissionError", e.getMessage());

        return "redirect:/home-trainer";
    }

    @ExceptionHandler(WorkoutReportNotFoundException.class)
    public String handleWorkoutReportNotFoundException(WorkoutReportNotFoundException e,
                                                       RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("notFoundError", e.getMessage());

        return "redirect:/reports/workouts";
    }

    @ExceptionHandler(MembershipReportNotFoundException.class)
    public String handleMembershipReportNotFoundException(MembershipReportNotFoundException e,
                                                          RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("notFoundError", e.getMessage());

        return "redirect:/reports/memberships";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            AccessDeniedException.class,
            AuthorizationDeniedException.class,
            NoResourceFoundException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class,
            CancelBookedWorkoutException.class
    })
    public ModelAndView handleNotFoundExceptions() {

        return new ModelAndView("not-found");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAnyException(Exception e) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("internal-server-error");
        modelAndView.addObject("errorMessage", e.getClass().getSimpleName());

        return modelAndView;
    }
}
