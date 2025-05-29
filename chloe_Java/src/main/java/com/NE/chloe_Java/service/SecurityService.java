package com.NE.chloe_Java.service;

import com.NE.chloe_Java.entity.Employee;
import com.NE.chloe_Java.entity.Payslip;
import com.NE.chloe_Java.repository.EmployeeRepository;
import com.NE.chloe_Java.repository.PayslipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final EmployeeRepository employeeRepository;
    private final PayslipRepository payslipRepository;

    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.toUpperCase()));
    }

    public boolean hasAnyRole(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        for (String role : roles) {
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.toUpperCase()))) {
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    public boolean isCurrentUser(String employeeCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        Optional<Employee> employee = employeeRepository.findByEmail(authentication.getName());
        return employee.map(e -> e.getCode().equals(employeeCode)).orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isPayslipOwner(String payslipId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        Optional<Employee> employee = employeeRepository.findByEmail(authentication.getName());
        if (employee.isEmpty()) return false;

        Optional<Payslip> payslip = payslipRepository.findById(payslipId);
        return payslip.map(p -> p.getEmployee().getCode().equals(employee.get().getCode())).orElse(false);
    }

    @Transactional(readOnly = true)
    public Set<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return Set.of();

        Optional<Employee> employee = employeeRepository.findByEmail(authentication.getName());
        return employee.map(Employee::getRoles).orElse(Set.of());
    }

    @Transactional(readOnly = true)
    public String getCurrentUserCode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;

        Optional<Employee> employee = employeeRepository.findByEmail(authentication.getName());
        return employee.map(Employee::getCode).orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean isDepartmentManager(String departmentCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        Optional<Employee> employee = employeeRepository.findByEmail(authentication.getName());
        if (employee.isEmpty()) return false;

        // Check if user has MANAGER role and is assigned to the department
        return hasRole("MANAGER") && employee.get().getEmployments().stream()
                .anyMatch(e -> e.getDepartment().equals(departmentCode));
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    @Transactional(readOnly = true)
    public boolean canAccessPayslip(String payslipId) {
        // Allow access if user is a manager or admin
        if (hasAnyRole("MANAGER", "ADMIN")) {
            return true;
        }

        // Allow access if user is the owner of the payslip
        return isPayslipOwner(payslipId);
    }

    @Transactional(readOnly = true)
    public boolean canManageEmployee(String employeeCode) {
        // Allow access if user is a manager or admin
        if (hasAnyRole("MANAGER", "ADMIN")) {
            return true;
        }

        // Allow access if user is accessing their own information
        return isCurrentUser(employeeCode);
    }

    @Transactional(readOnly = true)
    public Employee getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;

        return employeeRepository.findByEmail(authentication.getName())
                .orElse(null);
    }

    public void validateAccess(String employeeCode) {
        if (!canManageEmployee(employeeCode)) {
            throw new SecurityException("Access denied");
        }
    }

    public void validatePayslipAccess(String payslipId) {
        if (!canAccessPayslip(payslipId)) {
            throw new SecurityException("Access denied");
        }
    }
}