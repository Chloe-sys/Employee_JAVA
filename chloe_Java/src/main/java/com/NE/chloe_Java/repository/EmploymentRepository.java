package com.NE.chloe_Java.repository;


import com.NE.chloe_Java.entity.Employment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmploymentRepository extends JpaRepository<Employment, String> {
    List<Employment> findByEmployeeCodeAndStatus(String employeeCode, Employment.EmploymentStatus status);
    Optional<Employment> findByEmployeeCodeAndStatusAndDepartment(String employeeCode,
                                                                  Employment.EmploymentStatus status, String department);
    List<Employment> findByStatus(Employment.EmploymentStatus status);
}