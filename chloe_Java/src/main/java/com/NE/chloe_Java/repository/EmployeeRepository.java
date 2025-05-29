package com.NE.chloe_Java.repository;



import com.NE.chloe_Java.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByCode(String code);
    boolean existsByEmail(String email);
    Optional<Employee> findByCodeAndStatus(String code, Employee.EmployeeStatus status);
}