package com.clinical.backend.service;

import com.clinical.backend.dto.message.MessageRequest;
import com.clinical.backend.dto.message.MessageResponse;
import com.clinical.backend.entity.Message;
import com.clinical.backend.entity.User;
import com.clinical.backend.enums.UserRole;
import com.clinical.backend.mapper.MessageMapper;
import com.clinical.backend.repository.MessageRepository;
import com.clinical.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Message Service Tests")
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private MessageService messageService;

    private User senderUser;
    private User recipientUser;
    private Message testMessage;
    private MessageRequest messageRequest;

    @BeforeEach
    void setUp() {
        // Create sender user
        senderUser = new User();
        senderUser.setId(1L);
        senderUser.setEmail("doctor@test.com");
        senderUser.setFullName("Dr. Smith");
        senderUser.setRole(UserRole.DOCTOR);

        // Create recipient user
        recipientUser = new User();
        recipientUser.setId(2L);
        recipientUser.setEmail("secretary@test.com");
        recipientUser.setFullName("Secretary Jane");
        recipientUser.setRole(UserRole.SECRETARY);

        // Create test message
        testMessage = Message.builder()
                .id(1L)
                .fromUser(senderUser)
                .toUser(recipientUser)
                .subject("Patient appointment")
                .body("Please schedule an appointment for patient John Doe")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        // Create message request
        messageRequest = new MessageRequest();
        messageRequest.setToUserId(2L);
        messageRequest.setSubject("Patient appointment");
        messageRequest.setBody("Please schedule an appointment for patient John Doe");
    }

    @Test
    @DisplayName("Should send message successfully")
    void testSendMessageSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(senderUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipientUser));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // Act
        MessageResponse response = messageService.sendMessage(1L, messageRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Dr. Smith", response.getFromUserName());
        assertEquals("Secretary Jane", response.getToUserName());
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    @DisplayName("Should throw exception when sender not found")
    void testSendMessageSenderNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(999L, messageRequest);
        });

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    @DisplayName("Should throw exception when recipient not found")
    void testSendMessageRecipientNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(senderUser));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        messageRequest.setToUserId(999L);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(1L, messageRequest);
        });

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    @DisplayName("Should create message with isRead=false by default")
    void testMessageCreatedAsUnread() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(senderUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipientUser));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message saved = invocation.getArgument(0);
            assertFalse(saved.getIsRead());
            return testMessage;
        });

        // Act
        messageService.sendMessage(1L, messageRequest);

        // Assert
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @DisplayName("Should get received messages for user")
    void testGetReceivedMessages() {
        // Arrange
        Message message2 = Message.builder()
                .id(2L)
                .fromUser(senderUser)
                .toUser(recipientUser)
                .subject("Another message")
                .body("Body text")
                .isRead(true)
                .build();

        when(messageRepository.findByToUserIdOrderByCreatedAtDesc(2L))
                .thenReturn(Arrays.asList(testMessage, message2));

        // Act
        List<MessageResponse> responses = messageService.getReceivedMessages(2L);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(messageRepository, times(1)).findByToUserIdOrderByCreatedAtDesc(2L);
    }

    @Test
    @DisplayName("Should get sent messages for user")
    void testGetSentMessages() {
        // Arrange
        Message message2 = Message.builder()
                .id(2L)
                .fromUser(senderUser)
                .toUser(recipientUser)
                .subject("Second sent message")
                .body("Body text")
                .build();

        when(messageRepository.findByFromUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(testMessage, message2));

        // Act
        List<MessageResponse> responses = messageService.getSentMessages(1L);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(messageRepository, times(1)).findByFromUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("Should get unread messages for user")
    void testGetUnreadMessages() {
        // Arrange
        when(messageRepository.findUnreadMessages(2L))
                .thenReturn(Arrays.asList(testMessage));

        // Act
        List<MessageResponse> responses = messageService.getUnreadMessages(2L);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertFalse(responses.get(0).getIsRead());
    }

    @Test
    @DisplayName("Should count unread messages correctly")
    void testCountUnreadMessages() {
        // Arrange
        when(messageRepository.countUnreadMessages(2L)).thenReturn(5L);

        // Act
        long count = messageService.countUnreadMessages(2L);

        // Assert
        assertEquals(5L, count);
        verify(messageRepository, times(1)).countUnreadMessages(2L);
    }

    @Test
    @DisplayName("Should mark message as read successfully")
    void testMarkAsReadSuccess() {
        // Arrange
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message saved = invocation.getArgument(0);
            assertTrue(saved.getIsRead());
            assertNotNull(saved.getReadAt());
            return saved;
        });

        // Act
        MessageResponse response = messageService.markAsRead(1L, 2L);

        // Assert
        assertNotNull(response);
        assertTrue(response.getIsRead());
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    @DisplayName("Should throw exception when marking non-existent message as read")
    void testMarkAsReadMessageNotFound() {
        // Arrange
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            messageService.markAsRead(999L, 2L);
        });

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    @DisplayName("Should throw exception when unauthorized user tries to read message")
    void testMarkAsReadUnauthorized() {
        // Arrange
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));

        // Act & Assert - User 3 tries to read message sent to User 2
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            messageService.markAsRead(1L, 3L);
        });

        assertTrue(exception.getMessage().contains("Not authorized"));
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    @DisplayName("Should return empty list when user has no received messages")
    void testGetReceivedMessagesEmpty() {
        // Arrange
        when(messageRepository.findByToUserIdOrderByCreatedAtDesc(2L))
                .thenReturn(Arrays.asList());

        // Act
        List<MessageResponse> responses = messageService.getReceivedMessages(2L);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    @DisplayName("Should include subject and body in response")
    void testMessageResponseContainsSubjectAndBody() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(senderUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipientUser));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // Act
        MessageResponse response = messageService.sendMessage(1L, messageRequest);

        // Assert
        assertEquals("Patient appointment", response.getSubject());
        assertEquals("Please schedule an appointment for patient John Doe", response.getBody());
    }

    @Test
    @DisplayName("Should include user names in response")
    void testMessageResponseIncludesUserNames() {
        // Arrange
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));

        // Act - Get message to verify response format
        when(messageRepository.findByToUserIdOrderByCreatedAtDesc(2L))
                .thenReturn(Arrays.asList(testMessage));
        List<MessageResponse> responses = messageService.getReceivedMessages(2L);

        // Assert
        assertNotNull(responses.get(0).getFromUserName());
        assertNotNull(responses.get(0).getToUserName());
        assertEquals("Dr. Smith", responses.get(0).getFromUserName());
        assertEquals("Secretary Jane", responses.get(0).getToUserName());
    }
}
