package com.fitnessapp.exception;

import com.fitnessapp.user.model.User;
import com.fitnessapp.user.model.UserRole;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.web.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.Base64;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final UserService userService;

    public GlobalExceptionHandler(UserService userService) {
        this.userService = userService;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ModelAndView handleUserAlreadyExistsException(UserAlreadyExistsException e,
                                                         HttpServletRequest request) {

        ModelAndView modelAndView = new ModelAndView("register");

        UserRole userRole = UserRole.valueOf(request.getParameter("userRole"));
        RegisterRequest registerRequest = new RegisterRequest(
                request.getParameter("email"), request.getParameter("password"),
                userRole);

        modelAndView.addObject("registerRequest", registerRequest);
        modelAndView.addObject("message", e.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(ImageUploadException.class)
    public ModelAndView handleImageUploadException(ImageUploadException e, HttpServletRequest request) {

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("error", e.getMessage());

        getUser(request, modelAndView);

        return modelAndView;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxUploadSizeExceededException(HttpServletRequest request) {

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("error", "The file is too large! The maximum allowed size is 5MB");

        getUser(request, modelAndView);

        return modelAndView;
    }

    private void getUser(HttpServletRequest request, ModelAndView modelAndView) {
        Principal principal = request.getUserPrincipal();

        String email = principal.getName();
        User user = userService.findByEmail(email);
        modelAndView.addObject("user", user);

        if (user.getProfilePicture() != null) {
            String base64Image = "data:image/jpeg;base64," +
                    Base64.getEncoder().encodeToString(user.getProfilePicture());
            modelAndView.addObject("profilePicture", base64Image);
        } else {
            modelAndView.addObject("profilePicture", "/images/Basic_Ui_(186).jpg");
        }
    }
}
