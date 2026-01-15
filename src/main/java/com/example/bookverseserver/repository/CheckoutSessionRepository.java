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
   * Delete expired checkout sessions that are older than the cutoff date.
   * Only deletes sessions that are not completed (status != 'COMPLETED').
   * @param cutoffDate Delete sessions created before this date
   * @return Number of deleted sessions
   */
  @Modifying
  @Query("DELETE FROM CheckoutSession cs WHERE cs.createdAt < :cutoffDate AND cs.status <> 'COMPLETED'")
  int deleteExpiredSessions(@Param("cutoffDate") LocalDateTime cutoffDate);
}
