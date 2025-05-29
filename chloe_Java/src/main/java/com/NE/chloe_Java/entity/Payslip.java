package com.NE.chloe_Java.entity;



import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "payslips")
public class Payslip {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private BigDecimal houseAmount;

    @Column(nullable = false)
    private BigDecimal transportAmount;

    @Column(nullable = false)
    private BigDecimal employeeTaxedAmount;

    @Column(nullable = false)
    private BigDecimal pensionAmount;

    @Column(nullable = false)
    private BigDecimal medicalInsuranceAmount;

    @Column(nullable = false)
    private BigDecimal otherTaxedAmount;

    @Column(nullable = false)
    private BigDecimal grossSalary;

    @Column(nullable = false)
    private BigDecimal netSalary;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayslipStatus status = PayslipStatus.PENDING;

    public enum PayslipStatus {
        PENDING,
        PAID
    }
}