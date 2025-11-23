package com.fitnessapp.web;

import com.fitnessapp.security.CustomUserDetails;
import com.fitnessapp.subscription.model.Subscription;
import com.fitnessapp.subscription.service.SubscriptionService;
import com.fitnessapp.user.model.User;
import com.fitnessapp.user.model.UserRole;
import com.fitnessapp.user.service.UserService;
import com.fitnessapp.web.dto.RegisterRequest;
import com.fitnessapp.workout.model.Workout;
import com.fitnessapp.workout.service.WorkoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IndexControllerUTest {

    private UserService userService;
    private WorkoutService workoutService;
    private SubscriptionService subscriptionService;
    private IndexController controller;

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);
        workoutService = Mockito.mock(WorkoutService.class);
        subscriptionService = Mockito.mock(SubscriptionService.class);
        controller = new IndexController(userService, workoutService, subscriptionService);
    }

    @Test
    void getIndexPage_whenAnonymous_returnsIndex() {
        String view = controller.getIndexPage(null);
        assertEquals("index", view);
    }

    @Test
    void getIndexPage_whenClient_redirectsClientHome() {
        CustomUserDetails cud = new CustomUserDetails(UUID.randomUUID(), "e@e.com", "pwd", UserRole.CLIENT, false);
        String view = controller.getIndexPage(cud);
        assertEquals("redirect:/home-client", view);
    }

    @Test
    void getIndexPage_whenTrainer_redirectsTrainerHome() {
        CustomUserDetails cud = new CustomUserDetails(UUID.randomUUID(), "e@e.com", "pwd", UserRole.TRAINER, true);
        String view = controller.getIndexPage(cud);
        assertEquals("redirect:/home-trainer", view);
    }

    @Test
    void getIndexPage_whenAdmin_redirectsAdminHome() {
        CustomUserDetails cud = new CustomUserDetails(UUID.randomUUID(), "e@e.com", "pwd", UserRole.ADMIN, false);
        String view = controller.getIndexPage(cud);
        assertEquals("redirect:/home-admin", view);
    }

    @Test
    void getRegisterPage_whenAnonymous_returnsRegisterWithEmptyForm() {
        ModelAndView mv = controller.getRegisterPage(null);
        assertEquals("register", mv.getViewName());
        assertTrue(mv.getModel().get("registerRequest") instanceof RegisterRequest);
    }

    @Test
    void getRegisterPage_whenClient_redirectsClientHome() {
        CustomUserDetails cud = new CustomUserDetails(UUID.randomUUID(), "e@e.com", "pwd", UserRole.CLIENT, false);
        ModelAndView mv = controller.getRegisterPage(cud);
        assertEquals("redirect:/home-client", mv.getViewName());
    }

    @Test
    void login_whenAnonymous_returnsLogin() {
        String view = controller.Login(null);
        assertEquals("login", view);
    }

    @Test
    void login_whenAdmin_redirectsAdminHome() {
        CustomUserDetails cud = new CustomUserDetails(UUID.randomUUID(), "e@e.com", "pwd", UserRole.ADMIN, false);
        String view = controller.Login(cud);
        assertEquals("redirect:/home-admin", view);
    }

    @Test
    void registerNewUser_whenBindingErrors_returnsRegisterView() {
        RegisterRequest req = new RegisterRequest("bad", "short", null);
        BindingResult errors = new BeanPropertyBindingResult(req, "registerRequest");
        errors.rejectValue("email", "invalid", "Invalid");

        ModelAndView mv = controller.registerNewUser(req, errors);
        assertEquals("register", mv.getViewName());
        verifyNoInteractions(userService);
    }

    @Test
    void registerNewUser_whenValid_callsServiceAndRedirectsToLogin() {
        RegisterRequest req = new RegisterRequest("test@example.com", "123456", UserRole.CLIENT);
        BindingResult errors = new BeanPropertyBindingResult(req, "registerRequest");

        ModelAndView mv = controller.registerNewUser(req, errors);
        assertEquals("redirect:/login", mv.getViewName());
        verify(userService).register(req);
    }

    @Test
    void getClientHomePage_buildsModelWithStats() {
        UUID uid = UUID.randomUUID();
        CustomUserDetails cud = new CustomUserDetails(uid, "e@e.com", "pwd", UserRole.CLIENT, false);
        User u = User.builder().id(uid).email("e@e.com").build();
        when(userService.getById(uid)).thenReturn(u);
        when(workoutService.getAllCompletedMonthWorkouts()).thenReturn(10);
        when(workoutService.getAllCompletedMonthWorkoutsForClient(u)).thenReturn(4);

        ModelAndView mv = controller.getClientHomePage(cud);
        assertEquals("home-client", mv.getViewName());
        assertEquals(u, mv.getModel().get("user"));
        assertEquals(4, mv.getModel().get("monthCompletedWorkoutsForClientCount"));
        assertEquals(10, mv.getModel().get("allCompletedMonthWorkouts"));
        assertEquals(40.0, (Double) mv.getModel().get("totalPercentage"), 0.001);
    }

    @Test
    void getTrainerHomePage_buildsModel() {
        UUID uid = UUID.randomUUID();
        CustomUserDetails cud = new CustomUserDetails(uid, "e@e.com", "pwd", UserRole.TRAINER, true);
        User u = User.builder().id(uid).email("e@e.com").build();
        when(userService.getById(uid)).thenReturn(u);
        when(workoutService.getUpcomingWorkoutsByTrainer(u)).thenReturn(List.of(Workout.builder().build()));
        when(workoutService.getMonthCompletedWorkoutsByTrainer(u)).thenReturn(List.of(Workout.builder().build(), Workout.builder().build()));
        when(workoutService.calculateMonthlyAttendancePercentage(u)).thenReturn(75.0);

        ModelAndView mv = controller.getTrainerHomePage(cud);
        assertEquals("home-trainer", mv.getViewName());
        assertEquals(u, mv.getModel().get("user"));
        assertEquals(2, mv.getModel().get("monthCompletedWorkoutsCount"));
        assertEquals(75.0, (Double) mv.getModel().get("monthlyAttendancePercentage"), 0.001);
    }

    @Test
    void getAdminHomePage_buildsModel() {
        when(userService.getPendingApproveTrainers()).thenReturn(List.of(User.builder().email("t@e.com").build()));
        when(subscriptionService.getAll()).thenReturn(List.of(Subscription.builder().build()));

        ModelAndView mv = controller.getAdminHomePage();
        assertEquals("home-admin", mv.getViewName());
        assertNotNull(mv.getModel().get("pendingApproveTrainers"));
        assertNotNull(mv.getModel().get("subscriptions"));
    }
}
