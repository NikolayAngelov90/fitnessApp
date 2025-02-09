package com.fitnessapp.web;

import com.fitnessapp.user.model.User;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.utils.ProfilePictureHelper;
import com.fitnessapp.web.dto.RegisterRequest;
import com.fitnessapp.web.dto.UserEditRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;

@Controller
public class IndexController {

    private final UserService userService;
    private final ProfilePictureHelper profilePictureHelper;

    public IndexController(UserService userService,
                           ProfilePictureHelper profilePictureHelper) {
        this.userService = userService;
        this.profilePictureHelper = profilePictureHelper;
    }

    @GetMapping("/")
    public String getIndexPage(Principal principal) {

        if (principal != null) {
            return "redirect:/home";
        }

        return "index";
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage(Principal principal) {

        if (principal != null) {
            return new ModelAndView("redirect:/home");
        }


        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("registerRequest", RegisterRequest.empty());

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerNewUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("register");
        }

        userService.register(registerRequest);

        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/login")
    public String Login(Principal principal) {

        if (principal != null) {
            return "redirect:/home";
        }

        return "login";
    }

    @GetMapping("/home")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView getHomePage(Authentication authentication) {

        String email = authentication.getName();
        User user = userService.findByEmail(email);

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("userEditRequest",
                new UserEditRequest(user.getFirstName(), user.getLastName()));
        modelAndView.addObject("profilePicture",
                profilePictureHelper.resolveProfilePicture(user));

        modelAndView.addObject("user", user);
        return modelAndView;
    }
}
