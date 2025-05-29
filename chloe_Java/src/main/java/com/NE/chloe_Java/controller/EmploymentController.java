package com.NE.chloe_Java.controller;

import com.NE.chloe_Java.dto.employment.EmploymentRequest;
import com.NE.chloe_Java.dto.employment.EmploymentResponse;
import com.NE.chloe_Java.service.EmploymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class EmploymentController {

    private final EmploymentService employmentService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Create new employment record",
            description = "Creates a new employment record for an employee")
    public ResponseEntity<EmploymentResponse> createEmployment(@Valid @RequestBody EmploymentRequest request) {
        return ResponseEntity.ok(employmentService.createEmployment(request));
    }

    @PutMapping("/{code}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update employment record",
            description = "Updates an existing employment record")
    public ResponseEntity<EmploymentResponse> updateEmployment(
            @PathVariable String code,
            @Valid @RequestBody EmploymentRequest request) {
        return ResponseEntity.ok(employmentService.updateEmployment(code, request));
    }

    @GetMapping("/{code}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Get employment record",
            description = "Retrieves an employment record by its code")
    public ResponseEntity<EmploymentResponse> getEmployment(@PathVariable String code) {
        return ResponseEntity.ok(employmentService.getEmployment(code));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Get active employments",
            description = "Retrieves all active employment records")
    public ResponseEntity<List<EmploymentResponse>> getActiveEmployments() {
        return ResponseEntity.ok(employmentService.getActiveEmployments());
    }

    @GetMapping("/employee/{employeeCode}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Get active employment by employee",
            description = "Retrieves active employment record for specific employee")
    public ResponseEntity<EmploymentResponse> getActiveEmploymentByEmployee(
            @PathVariable String employeeCode) {
        return ResponseEntity.ok(employmentService.getActiveEmploymentByEmployee(employeeCode));
    }
}