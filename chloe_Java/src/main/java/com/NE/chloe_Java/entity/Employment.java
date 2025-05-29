package com.NE.chloe_Java.entity;


import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "employments")
public class Employment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private BigDecimal baseSalary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmploymentStatus status = EmploymentStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDate joiningDate;

    public enum EmploymentStatus {
        ACTIVE,
        INACTIVE
    }
}