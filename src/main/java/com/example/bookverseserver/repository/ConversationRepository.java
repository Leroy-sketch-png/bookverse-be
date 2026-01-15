package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Messaging.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    /**
     * Find all conversations where user is either buyer or seller.
     * Ordered by last message time descending.
     */
    @Query("""
        SELECT c FROM Conversation c
        LEFT JOIN FETCH c.buyer
        LEFT JOIN FETCH c.seller
        LEFT JOIN FETCH c.listing l
        LEFT JOIN FETCH l.bookMeta
        WHERE c.buyer.id = :userId OR c.seller.id = :userId
        ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdAt DESC
        """)
    Page<Conversation> findAllByParticipant(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find existing conversation between buyer and seller for a specific listing.
     */
    @Query("""
        SELECT c FROM Conversation c
        WHERE c.buyer.id = :buyerId 
        AND c.seller.id = :sellerId 
        AND (c.listing.id = :listingId OR (:listingId IS NULL AND c.listing IS NULL))
        """)
    Optional<Conversation> findByBuyerAndSellerAndListing(
        @Param("buyerId") Long buyerId,
        @Param("sellerId") Long sellerId,
        @Param("listingId") Long listingId
    );
    
    /**
     * Find conversation by ID with participants loaded.
     */
    @Query("""
        SELECT c FROM Conversation c
        LEFT JOIN FETCH c.buyer b
        LEFT JOIN FETCH b.profile
        LEFT JOIN FETCH c.seller s
        LEFT JOIN FETCH s.profile
        LEFT JOIN FETCH c.listing l
        LEFT JOIN FETCH l.bookMeta
        LEFT JOIN FETCH c.order
        WHERE c.id = :id
        """)
    Optional<Conversation> findByIdWithDetails(@Param("id") Long id);
    
    /**
     * Count total unread messages for a user across all conversations.
     */
    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN c.buyer.id = :userId THEN c.buyerUnreadCount 
                ELSE c.sellerUnreadCount 
            END
        ), 0)
        FROM Conversation c
        WHERE c.buyer.id = :userId OR c.seller.id = :userId
        """)
    int countTotalUnread(@Param("userId") Long userId);
}
