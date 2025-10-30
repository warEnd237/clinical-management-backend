package com.clinical.backend.repository;

import com.clinical.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findByToUserIdOrderByCreatedAtDesc(Long toUserId);
    
    List<Message> findByFromUserIdOrderByCreatedAtDesc(Long fromUserId);
    
    @Query("SELECT m FROM Message m WHERE m.toUser.id = :userId AND m.isRead = false ORDER BY m.createdAt DESC")
    List<Message> findUnreadMessages(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.toUser.id = :userId AND m.isRead = false")
    long countUnreadMessages(@Param("userId") Long userId);
}
