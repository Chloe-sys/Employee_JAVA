package com.NE.chloe_Java.service;



import com.NE.chloe_Java.dto.message.MessageResponse;
import com.NE.chloe_Java.entity.Employee;
import com.NE.chloe_Java.entity.Message;
import com.NE.chloe_Java.repository.EmployeeRepository;
import com.NE.chloe_Java.repository.MessageRepository;
import com.NE.chloe_Java.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public MessageResponse createSalaryNotification(String employeeCode, String subject, String content) {
        Employee employee = employeeRepository.findByCode(employeeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with code: " + employeeCode));

        Message message = Message.builder()
                .employee(employee)
                .subject(subject)
                .content(content)
                .build();

        Message savedMessage = messageRepository.save(message);
        return mapToResponse(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getEmployeeMessages(String employeeCode) {
        return messageRepository.findByEmployeeCodeOrderByCreatedAtDesc(employeeCode)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getUnreadMessages(String employeeCode) {
        return messageRepository.findByEmployeeCodeAndIsReadOrderByCreatedAtDesc(employeeCode, false)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageResponse markAsRead(String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        message.setRead(true);
        message.setReadAt(LocalDateTime.now());
        return mapToResponse(messageRepository.save(message));
    }

    @Transactional(readOnly = true)
    public long getUnreadMessageCount(String employeeCode) {
        return messageRepository.countByEmployeeCodeAndIsRead(employeeCode, false);
    }

    private MessageResponse mapToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .employeeCode(message.getEmployee().getCode())
                .employeeName(message.getEmployee().getFirstName() + " " + message.getEmployee().getLastName())
                .subject(message.getSubject())
                .content(message.getContent())
                .isRead(message.isRead())
                .createdAt(message.getCreatedAt())
                .readAt(message.getReadAt())
                .build();
    }
}
