package com.clinical.backend.controller;

import com.clinical.backend.dto.common.ApiResponse;
import com.clinical.backend.dto.message.MessageRequest;
import com.clinical.backend.dto.message.MessageResponse;
import com.clinical.backend.security.CustomUserDetails;
import com.clinical.backend.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Internal messaging endpoints")
public class MessageController {
    
    private final MessageService messageService;
    
    @PostMapping
    @Operation(summary = "Send message", description = "Send a message to another user")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @Valid @RequestBody MessageRequest request,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        MessageResponse message = messageService.sendMessage(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent successfully", message));
    }
    
    @GetMapping("/received")
    @Operation(summary = "Get received messages", description = "Get all messages received by the current user")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getReceivedMessages(
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        List<MessageResponse> messages = messageService.getReceivedMessages(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }
    
    @GetMapping("/sent")
    @Operation(summary = "Get sent messages", description = "Get all messages sent by the current user")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getSentMessages(
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        List<MessageResponse> messages = messageService.getSentMessages(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }
    
    @GetMapping("/unread")
    @Operation(summary = "Get unread messages", description = "Get all unread messages for the current user")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getUnreadMessages(
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        List<MessageResponse> messages = messageService.getUnreadMessages(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }
    
    @GetMapping("/unread/count")
    @Operation(summary = "Count unread messages", description = "Get count of unread messages")
    public ResponseEntity<ApiResponse<Long>> countUnreadMessages(
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        long count = messageService.countUnreadMessages(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark message as read", description = "Mark a message as read")
    public ResponseEntity<ApiResponse<MessageResponse>> markAsRead(
            @PathVariable Long id,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        MessageResponse message = messageService.markAsRead(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Message marked as read", message));
    }
}
