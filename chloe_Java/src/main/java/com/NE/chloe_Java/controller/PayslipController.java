package com.NE.chloe_Java.controller;

import com.NE.chloe_Java.dto.payslip.PayslipRequest;
import com.NE.chloe_Java.dto.payslip.PayslipResponse;
import com.NE.chloe_Java.service.PayslipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payslips")
@Tag(name = "Payslip Management", description = "Payslip management APIs")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class PayslipController {

    private final PayslipService payslipService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Generate payslip", description = "Generates a new payslip for an employee")
    public ResponseEntity<PayslipResponse> generatePayslip(@Valid @RequestBody PayslipRequest request) {
        return ResponseEntity.ok(payslipService.generatePayslip(request));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve payslip", description = "Approves a payslip and marks it as paid")
    public ResponseEntity<PayslipResponse> approvePayslip(@PathVariable String id) {
        return ResponseEntity.ok(payslipService.approvePayslip(id));
    }

    @GetMapping("/employee/{employeeCode}")
    @PreAuthorize("hasRole('MANAGER') or @securityService.isCurrentUser(#employeeCode)")
    @Operation(summary = "Get employee payslips", description = "Retrieves all payslips for a specific employee")
    public ResponseEntity<List<PayslipResponse>> getPayslipsByEmployee(@PathVariable String employeeCode) {
        return ResponseEntity.ok(payslipService.getPayslipsByEmployee(employeeCode));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get pending payslips", description = "Retrieves all pending payslips for a specific month and year")
    public ResponseEntity<List<PayslipResponse>> getPendingPayslips(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return ResponseEntity.ok(payslipService.getPayslipsByMonthAndYear(month, year));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('MANAGER') or @securityService.isPayslipOwner(#id)")
    @Operation(summary = "Download payslip", description = "Downloads payslip as PDF")
    public ResponseEntity<Resource> downloadPayslip(@PathVariable String id) {
        return payslipService.downloadPayslip(id);
    }
}