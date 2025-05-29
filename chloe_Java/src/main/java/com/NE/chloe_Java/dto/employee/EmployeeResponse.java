package com.NE.chloe_Java.dto.employee;


import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Data
public class EmployeeResponse {
    private String code;
    private String firstName;
    private String lastName;
    private String email;
    private Set<String> roles;
    private String mobile;
    private LocalDate dateOfBirth;
    private String status;
}