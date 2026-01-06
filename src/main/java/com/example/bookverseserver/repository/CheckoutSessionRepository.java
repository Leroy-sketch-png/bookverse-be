package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Order_Payment.CheckoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CheckoutSessionRepository extends JpaRepository<CheckoutSession, Long> {
  Optional<CheckoutSession> findByPaymentIntentId(String paymentIntentId);
}
