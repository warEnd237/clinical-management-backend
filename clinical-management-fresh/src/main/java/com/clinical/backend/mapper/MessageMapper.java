package com.clinical.backend.mapper;

import com.clinical.backend.dto.message.MessageResponse;
import com.clinical.backend.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {
    
    public MessageResponse toResponse(Message message) {
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
}
