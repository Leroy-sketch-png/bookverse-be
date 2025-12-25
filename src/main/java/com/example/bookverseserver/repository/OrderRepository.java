package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
  Page<Order> findAllByUser(User user, Pageable pageable);

  Page<Order> findAllByUserAndStatus(User user, OrderStatus status, Pageable pageable);

  Optional<Order> findByIdAndUser(UUID id, User user);
}
