package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Order_Payment.Payment;
import com.example.bookverseserver.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentIntentId(String paymentIntentId);

    // Find by order ID
    List<Payment> findByOrderId(Long orderId);

    // Tìm theo User ID (Vì Payment map User object, ta dùng user.id)
    Page<Payment> findByUserId(Long userId, Pageable pageable);
    
    // Platform revenue: sum of all PAID payments
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal sumAmountByStatus(PaymentStatus status);
    
    // Platform revenue in a date range (for trend calculations)
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status AND p.createdAt >= :start AND p.createdAt < :end")
    BigDecimal sumAmountByStatusAndDateRange(
            @Param("status") PaymentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    
    // Count by status
    long countByStatus(PaymentStatus status);
}