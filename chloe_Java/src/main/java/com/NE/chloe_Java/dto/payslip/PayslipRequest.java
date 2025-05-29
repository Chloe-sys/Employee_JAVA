package com.NE.chloe_Java.dto.payslip;


import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayslipRequest {
    @NotBlank(message = "Employee code is required")
    private String employeeCode;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotNull(message = "Year is required")
    @Min(value = 2024, message = "Year must be 2024 or later")
    private Integer year;

    private BigDecimal basicSalary;
    private BigDecimal allowances;
    private BigDecimal deductions;

}