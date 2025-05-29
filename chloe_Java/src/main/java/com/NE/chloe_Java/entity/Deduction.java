package com.NE.chloe_Java.entity;


import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "deductions")
public class Deduction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String code;

    @Column(nullable = false, unique = true)
    private String deductionName;

    @Column(nullable = false)
    private BigDecimal percentage;
}
