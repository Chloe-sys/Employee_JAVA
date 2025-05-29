package com.NE.chloe_Java.service;

import com.NE.chloe_Java.dto.payslip.PayslipRequest;
import com.NE.chloe_Java.dto.payslip.PayslipResponse;
import com.NE.chloe_Java.entity.*;
import com.NE.chloe_Java.exception.ResourceNotFoundException;
import com.NE.chloe_Java.repository.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayslipService {

    // Predefined deduction rates
    private static final BigDecimal EMPLOYEE_TAX_RATE = new BigDecimal("30.00");
    private static final BigDecimal PENSION_RATE = new BigDecimal("6.00"); // Updated from 3% to 6%
    private static final BigDecimal MEDICAL_INSURANCE_RATE = new BigDecimal("5.00");
    private static final BigDecimal HOUSING_RATE = new BigDecimal("14.00");
    private static final BigDecimal TRANSPORT_RATE = new BigDecimal("14.00");
    private static final BigDecimal OTHERS_RATE = new BigDecimal("5.00");

    private final PayslipRepository payslipRepository;
    private final EmployeeRepository employeeRepository;
    private final EmploymentRepository employmentRepository;
    private final DeductionRepository deductionRepository;
    private final MessageService messageService;
    private final SecurityService securityService;
    private final EmailService emailService;



    @Transactional
    public PayslipResponse generatePayslip(PayslipRequest request) {
        if (!securityService.hasRole("MANAGER")) {
            throw new AccessDeniedException("Only managers can generate payslips");
        }

        // Check for existing payslip
        if (payslipRepository.existsByEmployeeCodeAndMonthAndYear(
                request.getEmployeeCode(), request.getMonth(), request.getYear())) {
            throw new IllegalArgumentException("Payslip already exists for this month and year");
        }

        // Get employee and employment details
        Employee employee = employeeRepository.findByCodeAndStatus(request.getEmployeeCode(),
                        Employee.EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Employment employment = employmentRepository.findByEmployeeCodeAndStatus(
                        request.getEmployeeCode(), Employment.EmploymentStatus.ACTIVE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Active employment not found"));

        BigDecimal baseSalary = employment.getBaseSalary();

        // Calculate allowances
        BigDecimal houseAmount = calculatePercentage(baseSalary, HOUSING_RATE);
        BigDecimal transportAmount = calculatePercentage(baseSalary, TRANSPORT_RATE);
        BigDecimal grossSalary = baseSalary.add(houseAmount).add(transportAmount);

        // Calculate deductions
        BigDecimal employeeTax = calculatePercentage(baseSalary, EMPLOYEE_TAX_RATE);
        BigDecimal pension = calculatePercentage(baseSalary, PENSION_RATE);
        BigDecimal medicalInsurance = calculatePercentage(baseSalary, MEDICAL_INSURANCE_RATE);
        BigDecimal others = calculatePercentage(baseSalary, OTHERS_RATE);

        BigDecimal totalDeductions = employeeTax.add(pension).add(medicalInsurance).add(others);
        BigDecimal netSalary = grossSalary.subtract(totalDeductions);

        // Create and save payslip
        Payslip payslip = new Payslip();
        payslip.setEmployee(employee);
        payslip.setHouseAmount(houseAmount);
        payslip.setTransportAmount(transportAmount);
        payslip.setEmployeeTaxedAmount(employeeTax);
        payslip.setPensionAmount(pension);
        payslip.setMedicalInsuranceAmount(medicalInsurance);
        payslip.setOtherTaxedAmount(others);
        payslip.setGrossSalary(grossSalary);
        payslip.setNetSalary(netSalary);
        payslip.setMonth(request.getMonth());
        payslip.setYear(request.getYear());
        payslip.setStatus(Payslip.PayslipStatus.PENDING);

        Payslip savedPayslip = payslipRepository.save(payslip);

        // Notify employee
        String message = String.format(
                "Dear %s, your salary for %s/%d has been processed.\n" +
                        "Gross Salary: RWF %,.2f\n" +
                        "Total Deductions: RWF %,.2f\n" +
                        "Net Salary: RWF %,.2f\n" +
                        "Status: PENDING",
                employee.getFirstName(),
                getMonthName(request.getMonth()),
                request.getYear(),
                grossSalary,
                totalDeductions,
                netSalary
        );

        messageService.createSalaryNotification(employee.getCode(), "Payslip Generated", message);

        return mapToPayslipResponse(savedPayslip);
    }

    private BigDecimal calculatePercentage(BigDecimal amount, BigDecimal percentage) {
        return amount.multiply(percentage).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }


    @Transactional
    public PayslipResponse approvePayslip(String payslipId) {
        if (!securityService.hasRole("ADMIN")) {
            throw new AccessDeniedException("Only admins can approve payslips");
        }

        Payslip payslip = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip not found"));

        if (payslip.getStatus() == Payslip.PayslipStatus.PAID) {
            throw new IllegalStateException("Payslip is already paid");
        }

        payslip.setStatus(Payslip.PayslipStatus.PAID);
        Payslip savedPayslip = payslipRepository.save(payslip);

        BigDecimal totalAllowances = payslip.getHouseAmount().add(payslip.getTransportAmount());
        BigDecimal totalDeductions = calculateTotalDeductions(payslip);

        // Create and save notification message
        notifyEmployee(payslip.getEmployee().getCode(), "Payslip Approved and Paid",
                createPayslipApprovalMessage(payslip, totalAllowances, totalDeductions));

        // Send email notification
        try {
            Employee employee = payslip.getEmployee();
            String emailContent = String.format(
                    "Dear %s,\n\n" +
                            "Your salary for %s/%d from Rwanda Government amounting to RWF %,.2f " +
                            "has been credited to your account %s successfully.\n\n" +
                            "Payment Details:\n" +
                            "Gross Salary: RWF %,.2f\n" +
                            "Total Allowances: RWF %,.2f\n" +
                            "Total Deductions: RWF %,.2f\n" +
                            "Net Salary: RWF %,.2f\n\n" +
                            "Best regards,\n" +
                            "Payroll Management System",
                    employee.getFirstName(),
                    getMonthName(payslip.getMonth()),
                    payslip.getYear(),
                    payslip.getNetSalary(),
                    employee.getCode(),
                    payslip.getGrossSalary(),
                    totalAllowances,
                    totalDeductions,
                    payslip.getNetSalary()
            );

            emailService.sendEmail(
                    employee.getEmail(),
                    "Salary Payment Notification - " + getMonthName(payslip.getMonth()) + " " + payslip.getYear(),
                    emailContent
            );
        } catch (MessagingException e) {
            log.error("Failed to send email notification for payslip {}: {}", payslipId, e.getMessage());
            // Don't throw exception - we don't want to roll back the transaction if email fails
        }

        return mapToPayslipResponse(savedPayslip);
    }


    @Transactional(readOnly = true)
    public List<PayslipResponse> getPayslipsByEmployee(String employeeCode) {
        if (!securityService.hasRole("MANAGER") && !securityService.isCurrentUser(employeeCode)) {
            throw new AccessDeniedException("Access denied");
        }

        return payslipRepository.findByEmployeeCodeAndMonthAndYear(employeeCode, null, null)
                .stream()
                .map(this::mapToPayslipResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PayslipResponse> getPayslipsByMonthAndYear(Integer month, Integer year) {
        if (!securityService.hasRole("MANAGER")) {
            throw new AccessDeniedException("Only managers can view pending payslips");
        }

        return payslipRepository.findByMonthAndYearAndStatus(month, year, Payslip.PayslipStatus.PENDING)
                .stream()
                .map(this::mapToPayslipResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadPayslip(String payslipId) {
        Payslip payslip = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip not found"));

        if (!securityService.hasRole("MANAGER") && !securityService.isPayslipOwner(payslipId)) {
            throw new AccessDeniedException("Access denied");
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();
            addPayslipContent(document, payslip);
            document.close();

            ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=payslip-" + payslip.getEmployee().getCode() +
                                    "-" + payslip.getMonth() + "-" + payslip.getYear() + ".pdf")
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    // Helper methods
    private void addPayslipContent(Document document, Payslip payslip) throws DocumentException {
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

        Paragraph title = new Paragraph("PAYSLIP", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Employee Details
        PdfPTable employeeTable = new PdfPTable(2);
        employeeTable.setWidthPercentage(100);
        addTableRow(employeeTable, "Employee Name", payslip.getEmployee().getFirstName() + " " +
                payslip.getEmployee().getLastName(), headerFont, normalFont);
        addTableRow(employeeTable, "Employee Code", payslip.getEmployee().getCode(), headerFont, normalFont);
        addTableRow(employeeTable, "Month/Year", getMonthName(payslip.getMonth()) + " " +
                payslip.getYear(), headerFont, normalFont);
        document.add(employeeTable);

        // Salary Details
        PdfPTable salaryTable = new PdfPTable(2);
        salaryTable.setWidthPercentage(100);
        salaryTable.setSpacingBefore(20);

        // Earnings
        addTableRow(salaryTable, "EARNINGS", "", headerFont, normalFont);
        addTableRow(salaryTable, "Basic Salary", formatCurrency(payslip.getGrossSalary()
                        .subtract(payslip.getHouseAmount()).subtract(payslip.getTransportAmount())),
                headerFont, normalFont);
        addTableRow(salaryTable, "House Allowance", formatCurrency(payslip.getHouseAmount()),
                headerFont, normalFont);
        addTableRow(salaryTable, "Transport Allowance", formatCurrency(payslip.getTransportAmount()),
                headerFont, normalFont);
        addTableRow(salaryTable, "Gross Salary", formatCurrency(payslip.getGrossSalary()),
                headerFont, normalFont);

        // Deductions
        addTableRow(salaryTable, "DEDUCTIONS", "", headerFont, normalFont);
        addTableRow(salaryTable, "Tax", formatCurrency(payslip.getEmployeeTaxedAmount()),
                headerFont, normalFont);
        addTableRow(salaryTable, "Pension", formatCurrency(payslip.getPensionAmount()),
                headerFont, normalFont);
        addTableRow(salaryTable, "Medical Insurance", formatCurrency(payslip.getMedicalInsuranceAmount()),
                headerFont, normalFont);
        addTableRow(salaryTable, "Other Deductions", formatCurrency(payslip.getOtherTaxedAmount()),
                headerFont, normalFont);
        addTableRow(salaryTable, "Total Deductions", formatCurrency(calculateTotalDeductions(payslip)),
                headerFont, normalFont);

        // Net Salary
        addTableRow(salaryTable, "NET SALARY", formatCurrency(payslip.getNetSalary()),
                headerFont, normalFont);

        document.add(salaryTable);
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));

        labelCell.setPadding(5);
        valueCell.setPadding(5);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("RWF %,.2f", amount);
    }

    private void notifyEmployee(String employeeCode, String subject, String content) {
        messageService.createSalaryNotification(employeeCode, subject, content);
    }

    private String createPayslipGenerationMessage(int month, int year, BigDecimal baseSalary,
                                                  BigDecimal totalAllowances, BigDecimal totalDeductions,
                                                  BigDecimal netSalary) {
        return String.format(
                "Your payslip for %s %d has been generated.\n" +
                        "Basic Salary: %s\n" +
                        "Total Allowances: %s\n" +
                        "Total Deductions: %s\n" +
                        "Net Salary: %s\n\n" +
                        "Status: %s",
                getMonthName(month),
                year,
                formatCurrency(baseSalary),
                formatCurrency(totalAllowances),
                formatCurrency(totalDeductions),
                formatCurrency(netSalary),
                Payslip.PayslipStatus.PENDING
        );
    }

    private String createPayslipApprovalMessage(Payslip payslip, BigDecimal totalAllowances,
                                                BigDecimal totalDeductions) {
        return String.format(
                "Your payslip for %s %d has been approved and paid.\n" +
                        "Gross Salary: %s\n" +
                        "Total Allowances: %s\n" +
                        "Total Deductions: %s\n" +
                        "Net Salary: %s\n" +
                        "Payment Date: %s",
                getMonthName(payslip.getMonth()),
                payslip.getYear(),
                formatCurrency(payslip.getGrossSalary()),
                formatCurrency(totalAllowances),
                formatCurrency(totalDeductions),
                formatCurrency(payslip.getNetSalary()),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
    }

    private BigDecimal calculateTotalDeductions(Payslip payslip) {
        return payslip.getEmployeeTaxedAmount()
                .add(payslip.getPensionAmount())
                .add(payslip.getMedicalInsuranceAmount())
                .add(payslip.getOtherTaxedAmount());
    }

    private BigDecimal getDeductionRate(String deductionName) {
        return deductionRepository.findByDeductionName(deductionName)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction not found: " + deductionName))
                .getPercentage();
    }

    private BigDecimal calculateAmount(BigDecimal base, BigDecimal rate) {
        return base.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private String getMonthName(int month) {
        return java.time.Month.of(month).toString();
    }

    private PayslipResponse mapToPayslipResponse(Payslip payslip) {
        Employment activeEmployment = employmentRepository.findByEmployeeCodeAndStatus(
                        payslip.getEmployee().getCode(), Employment.EmploymentStatus.ACTIVE)
                .stream()
                .findFirst()
                .orElse(null);

        PayslipResponse response = new PayslipResponse();
        response.setId(payslip.getId());
        response.setEmployeeCode(payslip.getEmployee().getCode());
        response.setEmployeeName(payslip.getEmployee().getFirstName() + " " +
                payslip.getEmployee().getLastName());
        response.setBaseSalary(activeEmployment != null ?
                activeEmployment.getBaseSalary() : BigDecimal.ZERO);
        response.setHouseAmount(payslip.getHouseAmount());
        response.setTransportAmount(payslip.getTransportAmount());
        response.setEmployeeTaxedAmount(payslip.getEmployeeTaxedAmount());
        response.setPensionAmount(payslip.getPensionAmount());
        response.setMedicalInsuranceAmount(payslip.getMedicalInsuranceAmount());
        response.setOtherTaxedAmount(payslip.getOtherTaxedAmount());
        response.setGrossSalary(payslip.getGrossSalary());
        response.setNetSalary(payslip.getNetSalary());
        response.setMonth(payslip.getMonth());
        response.setYear(payslip.getYear());
        response.setStatus(payslip.getStatus().name());
        return response;
    }
}