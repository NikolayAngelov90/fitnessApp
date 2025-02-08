package com.fitnessapp.web;

import com.fitnessapp.user.model.User;
import com.fitnessapp.user.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/home")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/upload-image")
    public String uploadPicture(@RequestParam("image") MultipartFile profilePicture, Principal principal,
                                RedirectAttributes redirectAttributes) {

        String email = principal.getName();
        User user = userService.findByEmail(email);

        userService.uploadProfilePicture(user.getId(), profilePicture);

        redirectAttributes.addFlashAttribute("message", "Profile picture uploaded successfully!");

        return "redirect:/home";
    }

}
