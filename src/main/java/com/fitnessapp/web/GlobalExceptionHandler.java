package com.fitnessapp.web;

import com.fitnessapp.exception.*;
import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.subscription.service.SubscriptionService;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.web.dto.RegisterRequest;
import com.fitnessapp.web.mapper.DtoMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final UserService userService;
    private final SubscriptionService subscriptionService;

    public GlobalExceptionHandler(UserService userService,
                                  SubscriptionService subscriptionService) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ModelAndView handleUserAlreadyExistsException(UserAlreadyExistsException e,
                                                         @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        User user = userService.getById(customUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView("register");
        modelAndView.addObject("error", e.getMessage());
        modelAndView.addObject("registerRequest", RegisterRequest.empty());
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @ExceptionHandler(ImageUploadException.class)
    public ModelAndView handleImageUploadException(ImageUploadException e,
                                                   @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        User user = userService.getById(customUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("error", e.getMessage());
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
    public ModelAndView handlePhoneNumberAlreadyExistsException(PhoneNumberAlreadyExistsException e,
                                                                @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        User user = userService.getById(customUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView("edit-menu");
        modelAndView.addObject("error", e.getMessage());
        modelAndView.addObject("userEditRequest", DtoMapper.mapUserToUserEditRequest(user));
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ModelAndView handlePaymentFailedException(PaymentFailedException e,
                                                     @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        UUID id = e.getSubscriptionId();
        Subscription subscription = subscriptionService.getById(id);
        User user = userService.getById(customUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView("subscription-payment");
        modelAndView.addObject("error", e.getMessage());
        modelAndView.addObject("subscription", subscription);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ModelAndView handlePaymentNotFoundException(PaymentNotFoundException e,
                                                       @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        ModelAndView modelAndView = new ModelAndView("payment-history");
        modelAndView.addObject("user", userService.getById(customUserDetails.getUserId()));
        modelAndView.addObject("message", e.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(BusinessException.class)
    public ModelAndView handleBusinessException(BusinessException e,
                                                @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        User user = userService.getById(customUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("user", user);
        modelAndView.addObject("stopMembershipError", e.getMessage());

        return modelAndView;
    }
}
