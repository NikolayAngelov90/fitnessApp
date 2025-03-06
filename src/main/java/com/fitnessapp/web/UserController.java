package com.fitnessapp.web;

import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.web.dto.UserEditRequest;
import com.fitnessapp.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/upload-image")
    public String uploadPicture(@RequestParam("image") MultipartFile profilePicture,
                                @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                RedirectAttributes redirectAttributes) {

        User user = userService.getById(customUserDetails.getUserId());

        userService.uploadProfilePicture(user.getId(), profilePicture);

        redirectAttributes.addFlashAttribute("message", "Profile picture uploaded successfully!");
        return "redirect:/home";
    }

    @GetMapping("/edit")
    public ModelAndView getEditProfilePage(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        User user = userService.getById(customUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView("edit-menu");
        modelAndView.addObject("userEditRequest", DtoMapper.mapUserToUserEditRequest(user));

        return modelAndView;
    }

    @PostMapping("/edit")
    public ModelAndView updateProfile(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                      @Valid UserEditRequest userEditRequest,
                                      BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("edit-menu");
        }

        User user = userService.getById(customUserDetails.getUserId());
        userService.updateUser(user.getId(), userEditRequest);

        ModelAndView modelAndView = new ModelAndView("edit-menu");
        modelAndView.addObject("user", user);
        modelAndView.addObject("message", "Profile updated successfully!");

        return modelAndView;
    }

    @GetMapping("/trainers")
    public ModelAndView getTrainersPage() {

        ModelAndView modelAndView = new ModelAndView("trainers");

        List<User> trainers = userService.getAllTrainers();

        modelAndView.addObject("trainers", trainers);

        return modelAndView;
    }
}
