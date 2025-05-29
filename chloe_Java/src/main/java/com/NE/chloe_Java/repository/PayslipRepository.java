package com.NE.chloe_Java.repository;

import com.NE.chloe_Java.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, String> {
    List<Payslip> findByEmployeeCodeAndMonthAndYear(String employeeCode, Integer month, Integer year);
    List<Payslip> findByMonthAndYearAndStatus(Integer month, Integer year, Payslip.PayslipStatus status);
    Optional<Payslip> findByEmployeeCodeAndMonthAndYearAndStatus(String employeeCode,
                                                                 Integer month, Integer year, Payslip.PayslipStatus status);
    boolean existsByEmployeeCodeAndMonthAndYear(String employeeCode, Integer month, Integer year);
}

