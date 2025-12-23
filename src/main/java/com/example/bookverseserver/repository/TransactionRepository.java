package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Order_Payment.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentIntentId(String paymentIntentId);

    // Tìm theo User ID (Vì Payment map User object, ta dùng user.id)
    Page<Payment> findByUserId(Long userId, Pageable pageable);
}