package com.NE.chloe_Java.dto.payslip;


import lombok.Data;
import java.math.BigDecimal;

@Data
public class PayslipResponse {
    private String id;
    private String employeeCode;
    private String employeeName;
    private BigDecimal baseSalary;
    private BigDecimal houseAmount;
    private BigDecimal transportAmount;
    private BigDecimal employeeTaxedAmount;
    private BigDecimal pensionAmount;
    private BigDecimal medicalInsuranceAmount;
    private BigDecimal otherTaxedAmount;
    private BigDecimal grossSalary;
    private BigDecimal netSalary;
    private Integer month;
    private Integer year;
    private String status;
}