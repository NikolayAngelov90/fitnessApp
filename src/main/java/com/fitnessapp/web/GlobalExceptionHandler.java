package com.fitnessapp.web;

import com.fitnessapp.exception.ImageUploadException;
import com.fitnessapp.exception.PhoneNumberAlreadyExistsException;
import com.fitnessapp.exception.UserAlreadyExistsException;
import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.web.dto.RegisterRequest;
import com.fitnessapp.web.mapper.DtoMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final UserService userService;

    public GlobalExceptionHandler(UserService userService) {
        this.userService = userService;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ModelAndView handleUserAlreadyExistsException(UserAlreadyExistsException e) {

        ModelAndView modelAndView = new ModelAndView("register");
        modelAndView.addObject("error", e.getMessage());
        modelAndView.addObject("registerRequest", RegisterRequest.empty());

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

        return modelAndView;
    }
}
