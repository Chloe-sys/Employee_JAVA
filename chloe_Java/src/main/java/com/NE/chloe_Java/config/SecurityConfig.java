package com.NE.chloe_Java.config;

import com.NE.chloe_Java.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/error"
                        ).permitAll()

                        // Employee management endpoints
                        .requestMatchers("/api/v1/employees/**").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers("/api/v1/employees/register").hasRole("MANAGER")
                        .requestMatchers("/api/v1/payslips/generate").hasRole("MANAGER")
                        .requestMatchers("/api/v1/employments").hasRole("MANAGER")
                        .requestMatchers("/api/v1/auth/register/admin").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/auth/register/manager").hasAuthority("ROLE_ADMIN")


                        // Payslip management endpoints
                        .requestMatchers("/api/v1/payslips/generate").hasRole("MANAGER")
                        .requestMatchers("/api/v1/payslips/*/approve").hasRole("ADMIN")
                        .requestMatchers("/api/v1/payslips/pending").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers("/api/v1/payslips/employee/**").authenticated()
                        .requestMatchers("/api/v1/payslips/*/download").authenticated()

                        // Department management endpoints
                        .requestMatchers("/api/v1/departments/**").hasRole("MANAGER")

                        // Employment management endpoints
                        .requestMatchers("/api/v1/employments/**").hasRole("MANAGER")

                        // Message management endpoints
                        .requestMatchers("/api/v1/messages/**").authenticated()

                        // Require authentication for all other endpoints
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}