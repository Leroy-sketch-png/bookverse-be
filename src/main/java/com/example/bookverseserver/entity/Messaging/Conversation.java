package com.example.bookverseserver.entity.Messaging;

import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.User.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a conversation thread between a buyer and seller.
 * A conversation can be linked to a specific listing and/or order for context.
 */
@Entity
@Table(name = "conversation", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"buyer_id", "seller_id", "listing_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Conversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    // ─────────────────────────────────────────────────────────────────────────
    // Participants
    // ─────────────────────────────────────────────────────────────────────────
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    User buyer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    User seller;
    
    // Denormalized for quick display (avoid joins in list queries)
    @Column(name = "buyer_name", length = 100)
    String buyerName;
    
    @Column(name = "buyer_avatar", length = 500)
    String buyerAvatar;
    
    @Column(name = "seller_name", length = 100)
    String sellerName;
    
    @Column(name = "seller_avatar", length = 500)
    String sellerAvatar;
    
    // ─────────────────────────────────────────────────────────────────────────
    // Context (optional links to listing/order)
    // ─────────────────────────────────────────────────────────────────────────
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id")
    Listing listing;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    Order order;
    
    // ─────────────────────────────────────────────────────────────────────────
    // Last Message Preview (for inbox display)
    // ─────────────────────────────────────────────────────────────────────────
    
    @Column(name = "last_message_preview", length = 200)
    String lastMessagePreview;
    
    @Column(name = "last_message_at")
    LocalDateTime lastMessageAt;
    
    @Column(name = "last_sender_id")
    Long lastSenderId;
    
    // ─────────────────────────────────────────────────────────────────────────
    // Unread Tracking
    // ─────────────────────────────────────────────────────────────────────────
    
    @Builder.Default
    @Column(name = "buyer_unread_count")
    Integer buyerUnreadCount = 0;
    
    @Builder.Default
    @Column(name = "seller_unread_count")
    Integer sellerUnreadCount = 0;
    
    // ─────────────────────────────────────────────────────────────────────────
    // Messages
    // ─────────────────────────────────────────────────────────────────────────
    
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    List<ChatMessage> messages = new ArrayList<>();
    
    // ─────────────────────────────────────────────────────────────────────────
    // Timestamps
    // ─────────────────────────────────────────────────────────────────────────
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
    
    // ─────────────────────────────────────────────────────────────────────────
    // Helper Methods
    // ─────────────────────────────────────────────────────────────────────────
    
    /**
     * Check if a user is a participant in this conversation.
     */
    public boolean isParticipant(Long userId) {
        return (buyer != null && buyer.getId().equals(userId)) ||
               (seller != null && seller.getId().equals(userId));
    }
    
    /**
     * Get the other party in the conversation relative to the given user.
     */
    public User getOtherParty(Long userId) {
        if (buyer != null && buyer.getId().equals(userId)) {
            return seller;
        }
        return buyer;
    }
    
    /**
     * Increment unread count for the recipient (not the sender).
     */
    public void incrementUnreadFor(Long senderId) {
        if (buyer != null && buyer.getId().equals(senderId)) {
            // Sender is buyer, increment seller's unread
            sellerUnreadCount = (sellerUnreadCount == null ? 0 : sellerUnreadCount) + 1;
        } else {
            // Sender is seller, increment buyer's unread
            buyerUnreadCount = (buyerUnreadCount == null ? 0 : buyerUnreadCount) + 1;
        }
    }
    
    /**
     * Clear unread count for a user.
     */
    public void clearUnreadFor(Long userId) {
        if (buyer != null && buyer.getId().equals(userId)) {
            buyerUnreadCount = 0;
        } else {
            sellerUnreadCount = 0;
        }
    }
    
    /**
     * Get unread count for a specific user.
     */
    public int getUnreadCountFor(Long userId) {
        if (buyer != null && buyer.getId().equals(userId)) {
            return buyerUnreadCount == null ? 0 : buyerUnreadCount;
        }
        return sellerUnreadCount == null ? 0 : sellerUnreadCount;
    }
}
