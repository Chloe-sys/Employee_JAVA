package com.NE.chloe_Java.controller;


import com.NE.chloe_Java.dto.employee.EmployeeRequest;
import com.NE.chloe_Java.dto.employee.EmployeeResponse;
import com.NE.chloe_Java.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee Management", description = "Employee management APIs")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all employees", description = "Retrieves a list of all active employees")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @securityService.isCurrentUser(#code)")
    @Operation(summary = "Get employee by code", description = "Retrieves employee details by their code")
    public ResponseEntity<EmployeeResponse> getEmployeeByCode(@PathVariable String code) {
        return ResponseEntity.ok(employeeService.getEmployeeByCode(code));
    }

    @PutMapping("/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @securityService.isCurrentUser(#code)")
    @Operation(summary = "Update employee", description = "Updates employee information")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable String code,
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(code, request));
    }

    @DeleteMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete employee", description = "Soft deletes an employee by setting their status to DISABLED")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String code) {
        employeeService.deleteEmployee(code);
        return ResponseEntity.noContent().build();
    }
}