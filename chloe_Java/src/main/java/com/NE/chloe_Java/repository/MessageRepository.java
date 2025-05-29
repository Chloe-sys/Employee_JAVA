package com.NE.chloe_Java.repository;

import com.NE.chloe_Java.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByEmployeeCodeOrderByCreatedAtDesc(String employeeCode);
    List<Message> findByEmployeeCodeAndIsReadOrderByCreatedAtDesc(String employeeCode, boolean isRead);
    long countByEmployeeCodeAndIsRead(String employeeCode, boolean isRead);
}
