package com.fitnessapp.security;

import com.fitnessapp.user.model.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String targetUrl = request.getContextPath();

        if (userDetails.getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_TRAINER"))) {

            if (!userDetails.isAdditionalTrainerDataCompleted()) {
                response.sendRedirect(targetUrl + "/users/edit");
                return;
            }

            targetUrl += "/home-trainer";
        } else if (userDetails.getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_CLIENT"))) {
            targetUrl += "/home-client";
        } else if (userDetails.getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            targetUrl += "/home-admin";
        }

        response.sendRedirect(targetUrl);
    }

    public static String validateRedirectEndpoint(CustomUserDetails customUserDetails) {
        String redirectPath = "redirect:/home-";
        if (customUserDetails.getRole() == UserRole.ADMIN) {
            redirectPath += "admin";
        } else if (customUserDetails.getRole() == UserRole.CLIENT) {
            redirectPath += "client";
        } else if (customUserDetails.getRole() == UserRole.TRAINER) {
            redirectPath += "trainer";
        }

        return redirectPath;
    }
}
