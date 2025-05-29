package com.NE.chloe_Java.dto.employment;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EmploymentResponse {
    private String code;
    private String employeeCode;
    private String employeeName;
    private BigDecimal baseSalary;
    private String position;
    private String department;
    private String status;
    private LocalDateTime joiningDate;
}