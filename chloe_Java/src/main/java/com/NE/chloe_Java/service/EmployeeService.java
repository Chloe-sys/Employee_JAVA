package com.NE.chloe_Java.service;


import com.NE.chloe_Java.dto.employee.EmployeeRequest;
import com.NE.chloe_Java.dto.employee.EmployeeResponse;
import com.NE.chloe_Java.entity.Employee;
import com.NE.chloe_Java.exception.ResourceNotFoundException;
import com.NE.chloe_Java.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByCode(String code) {
        Employee employee = employeeRepository.findByCodeAndStatus(code, Employee.EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with code: " + code));
        return mapToEmployeeResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(String code, EmployeeRequest request) {
        Employee employee = employeeRepository.findByCodeAndStatus(code, Employee.EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with code: " + code));

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setMobile(request.getMobile());
        employee.setDateOfBirth(request.getDateOfBirth());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            employee.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        Employee updatedEmployee = employeeRepository.save(employee);
        return mapToEmployeeResponse(updatedEmployee);
    }

    @Transactional
    public void deleteEmployee(String code) {
        Employee employee = employeeRepository.findByCodeAndStatus(code, Employee.EmployeeStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with code: " + code));

        employee.setStatus(Employee.EmployeeStatus.DISABLED);
        employeeRepository.save(employee);
    }

    private EmployeeResponse mapToEmployeeResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setCode(employee.getCode());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setEmail(employee.getEmail());
        response.setRoles(employee.getRoles());
        response.setMobile(employee.getMobile());
        response.setDateOfBirth(employee.getDateOfBirth());
        response.setStatus(employee.getStatus().name());
        return response;
    }
}