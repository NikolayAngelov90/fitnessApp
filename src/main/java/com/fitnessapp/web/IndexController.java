package com.fitnessapp.web;

import com.fitnessapp.user.model.User;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Base64;

@Controller
public class IndexController {

    private final UserService userService;

    public IndexController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String getIndexPage() {

        return "index";
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {

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
    public String Login() {

        return "login";
    }

    @GetMapping("/home")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView getHomePage(Authentication authentication) {

        String email = authentication.getName();
        User user = userService.findByEmail(email);

        ModelAndView modelAndView = new ModelAndView("home");

        if (user.getProfilePicture() != null) {
            String base64Image = "data:image/jpeg;base64," +
                    Base64.getEncoder().encodeToString(user.getProfilePicture());
            modelAndView.addObject("profilePicture", base64Image);
        } else {
            modelAndView.addObject("profilePicture", "/images/Basic_Ui_(186).jpg");
        }

        modelAndView.addObject("user", user);
        return modelAndView;
    }
}
