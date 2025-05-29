package com.NE.chloe_Java.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String code;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "employee_roles", joinColumns = @JoinColumn(name = "employee_code"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    @Column(nullable = false)
    private String mobile;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @OneToMany(mappedBy = "employee")
    private List<Employment> employments;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    public enum EmployeeStatus {
        ACTIVE,
        DISABLED
    }
}
