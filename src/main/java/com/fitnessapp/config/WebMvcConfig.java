package com.fitnessapp.config;

import com.fitnessapp.security.CustomAuthenticationSuccessHandler;
import com.fitnessapp.security.TrainerDataCompletionFilter;
import com.fitnessapp.security.UserInterceptor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableMethodSecurity
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserInterceptor userInterceptor;
    private final CustomAuthenticationSuccessHandler customSuccessHandler;
    private final TrainerDataCompletionFilter trainerDataCompletionFilter;

    public WebMvcConfig(UserInterceptor userInterceptor,
                        CustomAuthenticationSuccessHandler customSuccessHandler,
                        TrainerDataCompletionFilter trainerDataCompletionFilter) {
        this.userInterceptor = userInterceptor;
        this.customSuccessHandler = customSuccessHandler;
        this.trainerDataCompletionFilter = trainerDataCompletionFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/trainers/**").hasRole("TRAINER")
                        .requestMatchers("/clients/**").hasRole("CLIENT")
                        .requestMatchers("/admins/**").hasRole("ADMIN")
                        .requestMatchers("/", "/register", "/plans", "/users/trainers", "/workouts").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(customSuccessHandler)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/"))
                .addFilterAfter(trainerDataCompletionFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userInterceptor);
    }
}
