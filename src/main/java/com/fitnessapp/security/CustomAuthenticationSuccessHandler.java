package com.fitnessapp.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (userDetails.getAuthorities()
                .stream()
                .allMatch(auth -> auth.getAuthority().equals("ROLE_TRAINER")) &&
                !userDetails.isAdditionalTrainerDataCompleted()) {
            response.sendRedirect(request.getContextPath() + "/users/edit");
        } else {
            response.sendRedirect(request.getContextPath() + "/home");
        }
    }
}
