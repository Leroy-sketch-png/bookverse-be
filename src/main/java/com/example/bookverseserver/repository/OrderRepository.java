package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
  Page<Order> findAllByUser(User user, Pageable pageable);

  Page<Order> findAllByUserAndStatus(User user, OrderStatus status, Pageable pageable);

  Optional<Order> findByIdAndUser(Long id, User user);

  Optional<Order> findById(Long id);
  
  /**
   * Find orders containing seller's listings with essential relations eagerly fetched.
   * Note: Only fetching items to avoid MultipleBagFetchException with payments/timeline.
   */
  @Query("SELECT DISTINCT o FROM Order o " +
         "LEFT JOIN FETCH o.items oi " +
         "LEFT JOIN FETCH oi.listing l " +
         "LEFT JOIN FETCH oi.seller s " +
         "LEFT JOIN FETCH s.userProfile " +
         "LEFT JOIN FETCH o.shippingAddress " +
         "WHERE oi.seller.id = :sellerId")
  List<Order> findOrdersBySellerId(@Param("sellerId") Long sellerId);
}
