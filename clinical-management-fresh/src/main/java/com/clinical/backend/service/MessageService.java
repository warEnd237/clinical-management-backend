package com.clinical.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinical.backend.dto.common.NotificationDto;
import com.clinical.backend.dto.message.MessageRequest;
import com.clinical.backend.dto.message.MessageResponse;
import com.clinical.backend.entity.Message;
import com.clinical.backend.entity.User;
import com.clinical.backend.mapper.MessageMapper;
import com.clinical.backend.repository.MessageRepository;
import com.clinical.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Transactional
    public MessageResponse sendMessage(Long fromUserId, MessageRequest request) {
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        User toUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));
        
        Message message = Message.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .subject(request.getSubject())
                .body(request.getBody())
                .isRead(false)
                .build();
        
        Message savedMessage = messageRepository.save(message);
        
        // Send WebSocket notification to recipient
        sendNotificationToUser(
            toUser.getEmail(),
            NotificationDto.messageReceived(
                toUser.getEmail(),
                savedMessage.getId(),
                fromUser.getFullName(),
                savedMessage.getSubject()
            )
        );
        
        return toResponse(savedMessage);
    }
    
    @Transactional(readOnly = true)
    public List<MessageResponse> getReceivedMessages(Long userId) {
        return messageRepository.findByToUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<MessageResponse> getSentMessages(Long userId) {
        return messageRepository.findByFromUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<MessageResponse> getUnreadMessages(Long userId) {
        return messageRepository.findUnreadMessages(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public long countUnreadMessages(Long userId) {
        return messageRepository.countUnreadMessages(userId);
    }
    
    @Transactional
    public MessageResponse markAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        if (!message.getToUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to read this message");
        }
        
        message.setIsRead(true);
        message.setReadAt(LocalDateTime.now());
        
        Message savedMessage = messageRepository.save(message);
        
        return toResponse(savedMessage);
    }
    
    private MessageResponse toResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .fromUserId(message.getFromUser().getId())
                .fromUserName(message.getFromUser().getFullName())
                .toUserId(message.getToUser().getId())
                .toUserName(message.getToUser().getFullName())
                .subject(message.getSubject())
                .body(message.getBody())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
    
    // Helper method to send WebSocket notifications
    private void sendNotificationToUser(String userEmail, NotificationDto notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                userEmail,
                "/queue/notifications",
                notification
            );
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to send WebSocket notification to " + userEmail + ": " + e.getMessage());
        }
    }
}
