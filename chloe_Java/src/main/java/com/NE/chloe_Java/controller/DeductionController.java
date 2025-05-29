package com.NE.chloe_Java.controller;

import com.NE.chloe_Java.dto.deduction.DeductionRequest;
import com.NE.chloe_Java.dto.deduction.DeductionResponse;
import com.NE.chloe_Java.service.DeductionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deductions")
@Tag(name = "Deduction Management", description = "Deduction management APIs")
@SecurityRequirement(name = "bearerAuth")
public class DeductionController {

    private final DeductionService deductionService;

    public DeductionController(DeductionService deductionService) {
        this.deductionService = deductionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create deduction", description = "Creates a new deduction for an employee")
    public ResponseEntity<DeductionResponse> createDeduction(@Valid @RequestBody DeductionRequest request) {
        return ResponseEntity.ok(deductionService.createDeduction(request));
    }

    @GetMapping("/employee/{employeeCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @securityService.isCurrentUser(#employeeCode)")
    @Operation(summary = "Get employee deductions", description = "Retrieves all deductions for a specific employee")
    public ResponseEntity<List<DeductionResponse>> getDeductionsByEmployee(@PathVariable String employeeCode) {
        return ResponseEntity.ok(deductionService.getDeductionsByEmployee(employeeCode));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get deduction by ID", description = "Retrieves deduction details by ID")
    public ResponseEntity<DeductionResponse> getDeductionById(@PathVariable String id) {
        return ResponseEntity.ok(deductionService.getDeductionById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update deduction", description = "Updates an existing deduction")
    public ResponseEntity<DeductionResponse> updateDeduction(
            @PathVariable String id,
            @Valid @RequestBody DeductionRequest request) {
        return ResponseEntity.ok(deductionService.updateDeduction(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete deduction", description = "Deletes a deduction")
    public ResponseEntity<Void> deleteDeduction(@PathVariable String id) {
        deductionService.deleteDeduction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get active deductions", description = "Retrieves all active deductions")
    public ResponseEntity<List<DeductionResponse>> getActiveDeductions() {
        return ResponseEntity.ok(deductionService.getActiveDeductions());
    }
}