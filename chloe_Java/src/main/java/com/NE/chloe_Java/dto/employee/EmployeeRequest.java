package com.NE.chloe_Java.dto.employee;



import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Data
public class EmployeeRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotEmpty(message = "At least one role is required")
    private Set<String> roles;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid mobile number format")
    private String mobile;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
}