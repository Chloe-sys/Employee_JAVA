package com.NE.chloe_Java.controller;

import com.NE.chloe_Java.dto.auth.AuthResponse;
import com.NE.chloe_Java.dto.auth.LoginRequest;
import com.NE.chloe_Java.dto.auth.RegisterRequest;
import com.NE.chloe_Java.dto.employee.EmployeeRequest;
import com.NE.chloe_Java.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "Authentication endpoints for registration and login")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/debug/me")
    public ResponseEntity<?> debugCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No authentication found");
        }

        Map<String, Object> details = new HashMap<>();
        details.put("principal", authentication.getPrincipal());
        details.put("authorities", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        details.put("isAuthenticated", authentication.isAuthenticated());

        return ResponseEntity.ok(details);
    }


    @Operation(summary = "Register new user", description = "Register as ADMIN/MANAGER/EMPLOYEE based on role")
    @PostMapping("/register")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @employeeRepository.count() == 0")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody EmployeeRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @Operation(summary = "Login user", description = "Authenticate and receive JWT token with role-based permissions")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .message("Invalid credentials")
                            .build());
        }
    }

    @Operation(summary = "Register manager", description = "Special endpoint for registering managers")
    @PostMapping("/register/manager")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AuthResponse> registerManager(@Valid @RequestBody EmployeeRequest request) {
        request.setRoles(Set.of("ROLE_MANAGER"));
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Register admin", description = "Special endpoint for registering admins")
    @PostMapping("/register/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AuthResponse> registerAdmin(@Valid @RequestBody EmployeeRequest request) {
        request.setRoles(Set.of("ROLE_ADMIN"));
        return ResponseEntity.ok(authService.register(request));
    }


    @Operation(summary = "Register employee", description = "Endpoint for registering regular employees")
    @PostMapping("/register/employee")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<AuthResponse> registerEmployee(@Valid @RequestBody EmployeeRequest request) {
        request.setRoles(Set.of("ROLE_EMPLOYEE"));
        return ResponseEntity.ok(authService.register(request));
    }
}