package com.fitnessapp.web;

import com.fitnessapp.user.model.User;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.utils.ProfilePictureHelper;
import com.fitnessapp.web.dto.UserEditRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final ProfilePictureHelper profilePictureHelper;

    public UserController(UserService userService,
                          ProfilePictureHelper profilePictureHelper) {
        this.userService = userService;
        this.profilePictureHelper = profilePictureHelper;
    }

    @PostMapping("/upload-image")
    public String uploadPicture(@RequestParam("image") MultipartFile profilePicture, Principal principal,
                                RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        User user = getUser(principal);
        userService.uploadProfilePicture(user.getId(), profilePicture);

        redirectAttributes.addFlashAttribute("message", "Profile picture uploaded successfully!");
        return "redirect:/home";
    }

    @GetMapping("/edit")
    public ModelAndView getEditProfilePage(Principal principal) {

        User user = getUser(principal);

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("user", user);
        modelAndView.addObject("profilePicture",
                profilePictureHelper.resolveProfilePicture(user));

        if (user.getFirstName() == null || user.getLastName() == null) {
            modelAndView.addObject("userEditRequest", UserEditRequest.empty());
        } else {
            modelAndView.addObject("userEditRequest",
                    new UserEditRequest(user.getFirstName(), user.getLastName()));
        }

        return modelAndView;
    }

    @PostMapping("/edit")
    public String updateProfile(@Valid UserEditRequest userEditRequest, BindingResult bindingResult,
                                Principal principal, RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please fix the errors in the form.");
            return "redirect:/edit#edit-profile-modal";
        }

        User user = getUser(principal);
        userService.updateUser(user.getId(), userEditRequest);

        redirectAttributes.addFlashAttribute("message", "Profile updated successfully!");
        return "redirect:/home";
    }

    private User getUser(Principal principal) {
        String email = principal.getName();
        return userService.findByEmail(email);
    }

}
