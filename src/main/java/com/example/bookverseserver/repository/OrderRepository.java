package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Order_Payment.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findById(Long id);
}
