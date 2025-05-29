package com.NE.chloe_Java.controller;


import com.NE.chloe_Java.dto.message.MessageResponse;
import com.NE.chloe_Java.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Message Management", description = "Message management APIs")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/employee/{employeeCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @securityService.isCurrentUser(#employeeCode)")
    @Operation(summary = "Get employee messages", description = "Retrieves all messages for a specific employee")
    public ResponseEntity<List<MessageResponse>> getEmployeeMessages(@PathVariable String employeeCode) {
        return ResponseEntity.ok(messageService.getEmployeeMessages(employeeCode));
    }

    @GetMapping("/employee/{employeeCode}/unread")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @securityService.isCurrentUser(#employeeCode)")
    @Operation(summary = "Get unread messages", description = "Retrieves all unread messages for a specific employee")
    public ResponseEntity<List<MessageResponse>> getUnreadMessages(@PathVariable String employeeCode) {
        return ResponseEntity.ok(messageService.getUnreadMessages(employeeCode));
    }

    @GetMapping("/employee/{employeeCode}/unread/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @securityService.isCurrentUser(#employeeCode)")
    @Operation(summary = "Get unread message count", description = "Retrieves the count of unread messages for a specific employee")
    public ResponseEntity<Long> getUnreadMessageCount(@PathVariable String employeeCode) {
        return ResponseEntity.ok(messageService.getUnreadMessageCount(employeeCode));
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or @securityService.isMessageOwner(#id)")
    @Operation(summary = "Mark message as read", description = "Marks a specific message as read")
    public ResponseEntity<MessageResponse> markAsRead(@PathVariable String id) {
        return ResponseEntity.ok(messageService.markAsRead(id));
    }
}
