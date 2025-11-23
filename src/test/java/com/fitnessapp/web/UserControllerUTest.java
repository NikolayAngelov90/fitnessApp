package com.fitnessapp.web;

import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.model.UserRole;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.web.dto.SwitchUserRoleRequest;
import com.fitnessapp.web.dto.UserEditRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserControllerUTest {

    private UserService userService;
    private UserController controller;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        controller = new UserController(userService);
    }

    @Test
    void uploadPicture_addsFlashMessage_andRedirectsByRole() {
        MultipartFile file = mock(MultipartFile.class);
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        UUID uid = UUID.randomUUID();
        CustomUserDetails cud = new CustomUserDetails(uid, "e@e.com", "pwd", UserRole.CLIENT, false);

        String view = controller.uploadPicture(file, cud, attrs);

        verify(userService).uploadProfilePicture(uid, file);
        assertEquals("Profile picture uploaded successfully!", attrs.getFlashAttributes().get("picMessage"));
        assertEquals("redirect:/home-client", view);
    }

    @Test
    void deletePicture_addsFlashMessage_andRedirectsByRole() {
        RedirectAttributes attrs = new RedirectAttributesModelMap();
        UUID uid = UUID.randomUUID();
        CustomUserDetails cud = new CustomUserDetails(uid, "e@e.com", "pwd", UserRole.TRAINER, true);

        String view = controller.deletePicture(cud, attrs);

        verify(userService).deleteProfilePicture(uid);
        assertEquals("Profile picture deleted successfully!", attrs.getFlashAttributes().get("picMessage"));
        assertEquals("redirect:/home-trainer", view);
    }

    @Test
    void getEditProfilePage_returnsEditMenuWithMappedRequest() {
        UUID uid = UUID.randomUUID();
        CustomUserDetails cud = new CustomUserDetails(uid, "e@e.com", "pwd", UserRole.CLIENT, false);
        when(userService.getById(uid)).thenReturn(User.builder().id(uid).email("e@e.com").build());

        ModelAndView mv = controller.getEditProfilePage(cud);
        assertEquals("edit-menu", mv.getViewName());
        assertNotNull(mv.getModel().get("userEditRequest"));
    }

    @Test
    void updateProfile_withErrors_returnsEditMenu() {
        UUID uid = UUID.randomUUID();
        CustomUserDetails cud = new CustomUserDetails(uid, "e@e.com", "pwd", UserRole.CLIENT, false);
        UserEditRequest req = new UserEditRequest("N", "L", "+1", "spec", "desc");
        BindingResult errors = new BeanPropertyBindingResult(req, "userEditRequest");
        errors.rejectValue("firstName", "invalid", "Invalid");

        ModelAndView mv = controller.updateProfile(cud, req, errors);
        assertEquals("edit-menu", mv.getViewName());
        verifyNoInteractions(userService);
    }

    @Test
    void updateProfile_success_updatesUser_andReturnsMessage() {
        UUID uid = UUID.randomUUID();
        CustomUserDetails cud = new CustomUserDetails(uid, "e@e.com", "pwd", UserRole.CLIENT, false);
        UserEditRequest req = new UserEditRequest("Name", "Last", "+1", "spec", "desc");
        BindingResult errors = new BeanPropertyBindingResult(req, "userEditRequest");

        ModelAndView mv = controller.updateProfile(cud, req, errors);
        assertEquals("edit-menu", mv.getViewName());
        assertEquals("Profile updated successfully!", mv.getModel().get("message"));
        verify(userService).updateUser(uid, req);
    }

    @Test
    void getTrainersPage_returnsTrainersList() {
        when(userService.getAllApprovedTrainers()).thenReturn(List.of(User.builder().email("t@e.com").build()));

        ModelAndView mv = controller.getTrainersPage();
        assertEquals("trainers", mv.getViewName());
        assertNotNull(mv.getModel().get("trainers"));
    }

    @Test
    void approveTrainer_callsService_andRedirects() {
        UUID id = UUID.randomUUID();
        String view = controller.approveTrainer(id);
        verify(userService).approveTrainer(id);
        assertEquals("redirect:/home-admin", view);
    }

    @Test
    void getAllUsers_returnsManageUsersWithSwitchRequest() {
        when(userService.getAllUsers()).thenReturn(List.of(User.builder().email("a@e.com").build()));

        ModelAndView mv = controller.getAllUsers();
        assertEquals("manage-users", mv.getViewName());
        assertNotNull(mv.getModel().get("users"));
        assertTrue(mv.getModel().get("switchUserRoleRequest") instanceof SwitchUserRoleRequest);
    }

    @Test
    void switchUserRole_withErrors_returnsManageUsersWithUsersList() {
        UUID id = UUID.randomUUID();
        SwitchUserRoleRequest req = new SwitchUserRoleRequest(UserRole.TRAINER);
        BindingResult errors = new BeanPropertyBindingResult(req, "switchUserRoleRequest");
        errors.rejectValue("userRole", "invalid", "Invalid");

        when(userService.getAllUsers()).thenReturn(List.of(User.builder().email("a@e.com").build()));

        ModelAndView mv = controller.switchUserRole(id, req, errors);
        assertEquals("manage-users", mv.getViewName());
        assertNotNull(mv.getModel().get("users"));
        verify(userService, never()).switchRole(any(), any());
    }

    @Test
    void switchUserRole_success_switchesAndRedirects() {
        UUID id = UUID.randomUUID();
        SwitchUserRoleRequest req = new SwitchUserRoleRequest(UserRole.TRAINER);
        BindingResult errors = new BeanPropertyBindingResult(req, "switchUserRoleRequest");

        ModelAndView mv = controller.switchUserRole(id, req, errors);
        assertEquals("redirect:/users", mv.getViewName());
        verify(userService).switchRole(id, req);
    }
}
