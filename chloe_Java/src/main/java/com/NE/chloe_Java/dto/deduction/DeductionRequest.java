package com.NE.chloe_Java.dto.deduction;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DeductionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String code;

    @Column(nullable = false, unique = true)
    private String deductionName;

    @Column(nullable = false)
    private BigDecimal percentage;

    // Predefined deduction types
    public static final String EMPLOYEE_TAX = "Employee Tax";
    public static final String PENSION = "Pension";
    public static final String MEDICAL_INSURANCE = "Medical Insurance";
    public static final String HOUSING = "Housing";
    public static final String TRANSPORT = "Transport";
    public static final String OTHERS = "Others";

    // Predefined percentages
    public static final BigDecimal EMPLOYEE_TAX_PERCENTAGE = new BigDecimal("30.0");
    public static final BigDecimal PENSION_PERCENTAGE = new BigDecimal("6.0");
    public static final BigDecimal MEDICAL_INSURANCE_PERCENTAGE = new BigDecimal("5.0");
    public static final BigDecimal HOUSING_PERCENTAGE = new BigDecimal("14.0");
    public static final BigDecimal TRANSPORT_PERCENTAGE = new BigDecimal("14.0");
    public static final BigDecimal OTHERS_PERCENTAGE = new BigDecimal("5.0");

}
