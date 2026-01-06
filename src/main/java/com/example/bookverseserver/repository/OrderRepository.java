package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
  Page<Order> findAllByUser(User user, Pageable pageable);

  Page<Order> findAllByUserAndStatus(User user, OrderStatus status, Pageable pageable);

  Optional<Order> findByIdAndUser(Long id, User user);

  Optional<Order> findById(Long id);
}
