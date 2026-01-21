package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Order_Payment.CheckoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CheckoutSessionRepository extends JpaRepository<CheckoutSession, Long> {
  Optional<CheckoutSession> findByPaymentIntentId(String paymentIntentId);
  
  /**
   * Find an existing pending checkout session for a cart.
   * Used to prevent duplicate sessions (cart_id has unique constraint).
   */
  @Query("SELECT cs FROM CheckoutSession cs WHERE cs.cart.id = :cartId AND cs.status = 'PENDING'")
  Optional<CheckoutSession> findPendingByCartId(@Param("cartId") Long cartId);
  
  /**
   * Find checkout session with fully loaded cart for order creation.
   * Eagerly loads: cart -> cartItems -> listing -> photos, bookMeta -> authors, images
   */
  @Query("SELECT DISTINCT cs FROM CheckoutSession cs " +
         "LEFT JOIN FETCH cs.user " +
         "LEFT JOIN FETCH cs.cart c " +
         "LEFT JOIN FETCH c.cartItems ci " +
         "LEFT JOIN FETCH ci.listing l " +
         "LEFT JOIN FETCH l.photos " +
         "LEFT JOIN FETCH l.seller " +
         "LEFT JOIN FETCH l.bookMeta bm " +
         "LEFT JOIN FETCH bm.authors " +
         "LEFT JOIN FETCH bm.images " +
         "WHERE cs.id = :sessionId")
  Optional<CheckoutSession> findByIdWithFullCart(@Param("sessionId") Long sessionId);
  
  /**
   * Delete expired checkout sessions that are older than the cutoff date.
   * Only deletes sessions that are not completed (status != 'COMPLETED').
   * @param cutoffDate Delete sessions created before this date
   * @return Number of deleted sessions
   */
  @Modifying
  @Query("DELETE FROM CheckoutSession cs WHERE cs.createdAt < :cutoffDate AND cs.status <> 'COMPLETED'")
  int deleteExpiredSessions(@Param("cutoffDate") LocalDateTime cutoffDate);
}
