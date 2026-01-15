package com.example.bookverseserver.entity.Messaging;

import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a single message within a conversation.
 */
@Entity
@Table(name = "chat_message", indexes = {
    @Index(name = "idx_message_conversation", columnList = "conversation_id, created_at DESC"),
    @Index(name = "idx_message_sender", columnList = "sender_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    // ─────────────────────────────────────────────────────────────────────────
    // Conversation Link
    // ─────────────────────────────────────────────────────────────────────────
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    Conversation conversation;
    
    // ─────────────────────────────────────────────────────────────────────────
    // Sender
    // ─────────────────────────────────────────────────────────────────────────
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    User sender;
    
    // Denormalized for quick display
    @Column(name = "sender_name", length = 100)
    String senderName;
    
    @Column(name = "sender_avatar", length = 500)
    String senderAvatar;
    
    // ─────────────────────────────────────────────────────────────────────────
    // Message Content
    // ─────────────────────────────────────────────────────────────────────────
    
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    String message;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 20)
    MessageType messageType = MessageType.TEXT;
    
    /**
     * Related entity ID for LISTING_SHARE or ORDER_UPDATE types.
     * - For LISTING_SHARE: the listing ID
     * - For ORDER_UPDATE: the order ID
     */
    @Column(name = "related_id")
    Long relatedId;
    
    // ─────────────────────────────────────────────────────────────────────────
    // Shared Content Preview (denormalized for display)
    // ─────────────────────────────────────────────────────────────────────────
    
    @Column(name = "shared_listing_title", length = 200)
    String sharedListingTitle;
    
    @Column(name = "shared_listing_image", length = 500)
    String sharedListingImage;
    
    @Column(name = "shared_listing_price")
    java.math.BigDecimal sharedListingPrice;
    
    // ─────────────────────────────────────────────────────────────────────────
    // Read Tracking
    // ─────────────────────────────────────────────────────────────────────────
    
    @Column(name = "read_at")
    LocalDateTime readAt;
    
    // ─────────────────────────────────────────────────────────────────────────
    // Timestamps
    // ─────────────────────────────────────────────────────────────────────────
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;
}
