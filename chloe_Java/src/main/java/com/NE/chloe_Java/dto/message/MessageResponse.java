package com.NE.chloe_Java.dto.message;


import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String id;
    private String employeeCode;
    private String employeeName;
    private String subject;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}