package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Messaging.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    /**
     * Find messages for a conversation, paginated, newest first.
     */
    @Query("""
        SELECT m FROM ChatMessage m
        LEFT JOIN FETCH m.sender
        WHERE m.conversation.id = :conversationId
        ORDER BY m.createdAt DESC
        """)
    Page<ChatMessage> findByConversationId(@Param("conversationId") Long conversationId, Pageable pageable);
    
    /**
     * Find all messages for a conversation (non-paginated), oldest first.
     */
    @Query("""
        SELECT m FROM ChatMessage m
        LEFT JOIN FETCH m.sender
        WHERE m.conversation.id = :conversationId
        ORDER BY m.createdAt ASC
        """)
    List<ChatMessage> findAllByConversationIdOrderByCreatedAtAsc(@Param("conversationId") Long conversationId);
    
    /**
     * Mark all unread messages as read for a user in a conversation.
     */
    @Modifying
    @Query("""
        UPDATE ChatMessage m
        SET m.readAt = :readAt
        WHERE m.conversation.id = :conversationId
        AND m.sender.id != :userId
        AND m.readAt IS NULL
        """)
    int markAsRead(
        @Param("conversationId") Long conversationId,
        @Param("userId") Long userId,
        @Param("readAt") LocalDateTime readAt
    );
    
    /**
     * Count unread messages in a conversation for a specific user.
     */
    @Query("""
        SELECT COUNT(m) FROM ChatMessage m
        WHERE m.conversation.id = :conversationId
        AND m.sender.id != :userId
        AND m.readAt IS NULL
        """)
    int countUnread(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
