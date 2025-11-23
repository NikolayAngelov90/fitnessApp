package com.fitnessapp.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomAuthenticationSuccessHandlerUTest {

    private final CustomAuthenticationSuccessHandler handler = new CustomAuthenticationSuccessHandler();

    private static Authentication auth(CustomUserDetails cud) {
        return new UsernamePasswordAuthenticationToken(cud, cud.getPassword(), cud.getAuthorities());
    }

    @Test
    void onAuthenticationSuccess_trainerWithoutCompletedData_redirectsToEdit() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        CustomUserDetails cud = new CustomUserDetails(UUID.randomUUID(), "t@e.com", "pwd", com.fitnessapp.user.model.UserRole.TRAINER, false);

        handler.onAuthenticationSuccess(req, res, auth(cud));

        assertEquals("/users/edit", res.getRedirectedUrl());
    }

    @Test
    void onAuthenticationSuccess_trainerWithCompletedData_redirectsHomeTrainer() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        CustomUserDetails cud = new CustomUserDetails(UUID.randomUUID(), "t@e.com", "pwd", com.fitnessapp.user.model.UserRole.TRAINER, true);

        handler.onAuthenticationSuccess(req, res, auth(cud));

        assertEquals("/home-trainer", res.getRedirectedUrl());
    }

    @Test
    void onAuthenticationSuccess_client_redirectsHomeClient() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        CustomUserDetails cud = new CustomUserDetails(UUID.randomUUID(), "c@e.com", "pwd", com.fitnessapp.user.model.UserRole.CLIENT, false);

        handler.onAuthenticationSuccess(req, res, auth(cud));

        assertEquals("/home-client", res.getRedirectedUrl());
    }

    @Test
    void onAuthenticationSuccess_admin_redirectsHomeAdmin() throws IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        CustomUserDetails cud = new CustomUserDetails(UUID.randomUUID(), "a@e.com", "pwd", com.fitnessapp.user.model.UserRole.ADMIN, false);

        handler.onAuthenticationSuccess(req, res, auth(cud));

        assertEquals("/home-admin", res.getRedirectedUrl());
    }

    @Test
    void validateRedirectEndpoint_mapsByRole() {
        CustomUserDetails admin = new CustomUserDetails(UUID.randomUUID(), "a@e.com", "pwd", com.fitnessapp.user.model.UserRole.ADMIN, false);
        CustomUserDetails client = new CustomUserDetails(UUID.randomUUID(), "c@e.com", "pwd", com.fitnessapp.user.model.UserRole.CLIENT, false);
        CustomUserDetails trainer = new CustomUserDetails(UUID.randomUUID(), "t@e.com", "pwd", com.fitnessapp.user.model.UserRole.TRAINER, true);

        assertEquals("redirect:/home-admin", CustomAuthenticationSuccessHandler.validateRedirectEndpoint(admin));
        assertEquals("redirect:/home-client", CustomAuthenticationSuccessHandler.validateRedirectEndpoint(client));
        assertEquals("redirect:/home-trainer", CustomAuthenticationSuccessHandler.validateRedirectEndpoint(trainer));
    }
}
