package com.fitnessapp.security;

import com.fitnessapp.user.model.User;
import com.fitnessapp.user.model.UserRole;
import com.fitnessapp.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class TrainerDataCompletionFilter extends OncePerRequestFilter {

    private final UserService userService;

    public TrainerDataCompletionFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        List<String> staticResources = List.of("/resources/", "/static/", "/css/", "/js/", "/images/");

        boolean isStaticResource = staticResources.stream().anyMatch(requestURI::startsWith);
        if (isStaticResource) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails userDetails) {

            User user = userService.getById(userDetails.getUserId());

            if (user.getRole() == UserRole.TRAINER && !user.isAdditionalTrainerDataCompleted() &&
                    !request.getRequestURI().equals(request.getContextPath() + "/users/edit")) {
                response.sendRedirect(request.getContextPath() + "/users/edit");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
