package com.NE.chloe_Java.service;

import com.NE.chloe_Java.dto.employment.EmploymentRequest;
import com.NE.chloe_Java.dto.employment.EmploymentResponse;
import com.NE.chloe_Java.entity.Employee;
import com.NE.chloe_Java.entity.Employment;
import com.NE.chloe_Java.exception.ResourceNotFoundException;
import com.NE.chloe_Java.repository.EmployeeRepository;
import com.NE.chloe_Java.repository.EmploymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmploymentService {

    private final EmploymentRepository employmentRepository;
    private final EmployeeRepository employeeRepository;
    private final SecurityService securityService;

    @Transactional
    public EmploymentResponse createEmployment(EmploymentRequest request) {
        if (!securityService.hasRole("MANAGER")) {
            throw new AccessDeniedException("Only administrators can create employment records");
        }

        // Check if employee exists
        Employee employee = employeeRepository.findByCodeAndStatus(
                        request.getEmployeeCode(), Employee.EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // Check if active employment already exists
        employmentRepository.findByEmployeeCodeAndStatus(
                        request.getEmployeeCode(), Employment.EmploymentStatus.ACTIVE)
                .stream()
                .findFirst()
                .ifPresent(e -> {
                    throw new IllegalStateException("Active employment already exists for this employee");
                });

        Employment employment = new Employment();
        employment.setEmployee(employee);
        employment.setBaseSalary(request.getBaseSalary());
        employment.setPosition(request.getPosition());
        employment.setDepartment(request.getDepartment());
        employment.setStatus(Employment.EmploymentStatus.ACTIVE);
        employment.setJoiningDate(LocalDate.now());

        Employment savedEmployment = employmentRepository.save(employment);
        return mapToEmploymentResponse(savedEmployment);
    }

    private EmploymentResponse mapToEmploymentResponse(Employment employment) {
        EmploymentResponse response = new EmploymentResponse();
        response.setCode(employment.getCode());
        response.setEmployeeCode(employment.getEmployee().getCode());
        response.setEmployeeName(employment.getEmployee().getFirstName() + " " +
                employment.getEmployee().getLastName());
        response.setBaseSalary(employment.getBaseSalary());
        response.setPosition(employment.getPosition());
        response.setDepartment(employment.getDepartment());
        response.setStatus(employment.getStatus().name());
        response.setJoiningDate(employment.getJoiningDate().atStartOfDay());
        return response;
    }

    @Transactional
    public EmploymentResponse updateEmployment(String code, EmploymentRequest request) {
        if (!securityService.hasRole("MANAGER")) {
            throw new AccessDeniedException("Only administrators can update employment records");
        }

        Employment employment = employmentRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("Employment not found"));

        employment.setBaseSalary(request.getBaseSalary());
        employment.setPosition(request.getPosition());
        employment.setDepartment(request.getDepartment());
        employment.setStatus(Employment.EmploymentStatus.valueOf(request.getStatus()));
//
//        if (employment.getStatus() == Employment.EmploymentStatus.INACTIVE) {
//            employment.setEndDate(LocalDateTime.now());
//        }

        Employment savedEmployment = employmentRepository.save(employment);
        return mapToEmploymentResponse(savedEmployment);
    }

    @Transactional(readOnly = true)
    public EmploymentResponse getEmployment(String code) {
        if (!securityService.hasRole("MANAGER") && !securityService.hasRole("ADMIN")) {
            throw new AccessDeniedException("Access denied");
        }

        Employment employment = employmentRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("Employment not found"));

        return mapToEmploymentResponse(employment);
    }

    @Transactional(readOnly = true)
    public List<EmploymentResponse> getActiveEmployments() {
        if (!securityService.hasRole("MANAGER") && !securityService.hasRole("ADMIN")) {
            throw new AccessDeniedException("Access denied");
        }

        return employmentRepository.findByStatus(Employment.EmploymentStatus.ACTIVE)
                .stream()
                .map(this::mapToEmploymentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmploymentResponse getActiveEmploymentByEmployee(String employeeCode) {
        if (!securityService.hasRole("MANAGER") && !securityService.hasRole("ADMIN")
                && !securityService.isCurrentUser(employeeCode)) {
            throw new AccessDeniedException("Access denied");
        }

        Employment employment = employmentRepository.findByEmployeeCodeAndStatus(
                        employeeCode, Employment.EmploymentStatus.ACTIVE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Active employment not found"));

        return mapToEmploymentResponse(employment);
    }
}