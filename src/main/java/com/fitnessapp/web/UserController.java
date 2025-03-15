package com.fitnessapp.web;

import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.web.dto.SwitchUserRoleRequest;
import com.fitnessapp.web.dto.UserEditRequest;
import com.fitnessapp.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

import static com.fitnessapp.security.CustomAuthenticationSuccessHandler.validateRedirectEndpoint;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/upload-image")
    public String uploadPicture(@RequestParam("image") MultipartFile profilePicture,
                                @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                RedirectAttributes redirectAttributes) {

        userService.uploadProfilePicture(customUserDetails.getUserId(), profilePicture);

        redirectAttributes.addFlashAttribute("picMessage", "Profile picture uploaded successfully!");

        return validateRedirectEndpoint(customUserDetails);
    }

    @DeleteMapping("/delete-image")
    public String deletePicture(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                RedirectAttributes redirectAttributes) {

        userService.deleteProfilePicture(customUserDetails.getUserId());

        redirectAttributes.addFlashAttribute("picMessage", "Profile picture deleted successfully!");

        return validateRedirectEndpoint(customUserDetails);
    }

    @GetMapping("/edit")
    public ModelAndView getEditProfilePage(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        User user = userService.getById(customUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView("edit-menu");
        modelAndView.addObject("userEditRequest", DtoMapper.mapUserToUserEditRequest(user));

        return modelAndView;
    }

    @PatchMapping("/edit")
    public ModelAndView updateProfile(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                      @Valid UserEditRequest userEditRequest,
                                      BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("edit-menu");
        }

        userService.updateUser(customUserDetails.getUserId(), userEditRequest);

        ModelAndView modelAndView = new ModelAndView("edit-menu");
        modelAndView.addObject("message", "Profile updated successfully!");

        return modelAndView;
    }

    @GetMapping("/trainers")
    public ModelAndView getTrainersPage() {

        ModelAndView modelAndView = new ModelAndView("trainers");

        List<User> trainers = userService.getAllApprovedTrainers();

        modelAndView.addObject("trainers", trainers);

        return modelAndView;
    }

    @PatchMapping("/{id}/approve-trainer")
    @PreAuthorize("hasRole('ADMIN')")
    public String approveTrainer(@PathVariable UUID id) {

        userService.approveTrainer(id);

        return "redirect:/home-admin";
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getAllUsers() {

        List<User> allUsers = userService.getAllUsers();

        ModelAndView modelAndView = new ModelAndView("manage-users");
        modelAndView.addObject("users", allUsers);
        modelAndView.addObject("switchUserRoleRequest", SwitchUserRoleRequest.empty());

        return modelAndView;
    }

    @PatchMapping("/{id}/update-role")
    @PreAuthorize("hasRole('ADMIN')")
    public String switchUserRole(@PathVariable UUID id,
                                 @ModelAttribute SwitchUserRoleRequest switchUserRoleRequest) {

        userService.switchRole(id, switchUserRoleRequest);

        return "redirect:/users";
    }
}
