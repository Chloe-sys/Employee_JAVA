package com.NE.chloe_Java.service;

import com.NE.chloe_Java.dto.auth.AuthResponse;
import com.NE.chloe_Java.dto.auth.LoginRequest;
import com.NE.chloe_Java.dto.auth.RegisterRequest;
import com.NE.chloe_Java.dto.employee.EmployeeRequest;
import com.NE.chloe_Java.entity.Employee;
import com.NE.chloe_Java.entity.EmployeeRole;
import com.NE.chloe_Java.exception.ResourceNotFoundException;
import com.NE.chloe_Java.repository.EmployeeRepository;
import com.NE.chloe_Java.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.flogger.Flogger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(EmployeeRequest request) {
        boolean isFirstUser = employeeRepository.count() == 0;

        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        Employee employee = new Employee();
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPassword(passwordEncoder.encode(request.getPassword()));
        employee.setMobile(request.getMobile());
        employee.setDateOfBirth(request.getDateOfBirth());

        // Set roles based on registration type
        Set<String> roles = new HashSet<>();
        if (isFirstUser) {
            // First user gets ADMIN and MANAGER roles
            roles.add(EmployeeRole.ROLE_ADMIN.name());
        } else if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            roles.addAll(request.getRoles());
        } else {
            roles.add(EmployeeRole.ROLE_EMPLOYEE.name());
        }

//        // Log the roles being set
//        Flogger.debug("Setting roles for new employee: {}", roles);

        employee.setRoles(roles);
        employee.setStatus(Employee.EmployeeStatus.ACTIVE);

        Employee savedEmployee = employeeRepository.save(employee);

        return AuthResponse.builder()
                .employeeCode(savedEmployee.getCode())
                .email(savedEmployee.getEmail())
                .roles(savedEmployee.getRoles())
                .message("Registration successful")
                .build();
    }


@Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            Employee employee = employeeRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

            if (employee.getStatus() != Employee.EmployeeStatus.ACTIVE) {
                throw new IllegalStateException("Account is not active");
            }

            String token = tokenProvider.generateToken(authentication);

            return AuthResponse.builder()
                    .token(token)
                    .employeeCode(employee.getCode())
                    .email(employee.getEmail())
                    .roles(employee.getRoles())
                    .message("Login successful")
                    .build();

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid email or password");
        }
    }

    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        return tokenProvider.validateToken(token);
    }

    public boolean isFirstTimeRegistration() {
        return employeeRepository.count() == 0;
    }

    private void validateRoleAssignment(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("At least one role must be specified");
        }

        for (String role : roles) {
            try {
                EmployeeRole.valueOf(role);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + role);
            }
        }
    }

    public AuthResponse registerWithRole(EmployeeRequest request, String role) {
        request.setRoles((Set<String>) Set.of(role));
        return register(request);
    }

    public AuthResponse registerManager(EmployeeRequest request) {
        return registerWithRole(request, EmployeeRole.ROLE_MANAGER.name());
    }

    public AuthResponse registerAdmin(EmployeeRequest request) {
        return registerWithRole(request, EmployeeRole.ROLE_ADMIN.name());
    }

    public AuthResponse registerEmployee(EmployeeRequest request) {
        return registerWithRole(request, EmployeeRole.ROLE_EMPLOYEE.name());
    }
}