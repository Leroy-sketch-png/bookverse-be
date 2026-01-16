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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
  Page<Order> findAllByUser(User user, Pageable pageable);

  Page<Order> findAllByUserAndStatus(User user, OrderStatus status, Pageable pageable);

  Optional<Order> findByIdAndUser(Long id, User user);

  Optional<Order> findById(Long id);
  
  /**
   * Count orders by status (for public stats).
   */
  long countByStatusIn(List<OrderStatus> statuses);
  
  /**
   * Count orders created after a date (for public stats - today's activity).
   */
  long countByCreatedAtAfter(LocalDateTime date);
  
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
  
  /**
   * Calculate average shipping time (hours) for a seller's orders.
   * Only considers orders that have been shipped (shippedAt is not null).
   * Uses native query because EXTRACT(EPOCH FROM interval) is PostgreSQL-specific.
   */
  @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (o.shipped_at - o.created_at)) / 3600) " +
         "FROM orders o JOIN order_items oi ON oi.order_id = o.id " +
         "WHERE oi.seller_id = :sellerId AND o.shipped_at IS NOT NULL", 
         nativeQuery = true)
  Double calculateAverageShippingTimeHours(@Param("sellerId") Long sellerId);
  
  /**
   * Count total orders for a seller (excluding cancelled).
   */
  @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.items oi " +
         "WHERE oi.seller.id = :sellerId AND o.status NOT IN :excludedStatuses")
  Long countOrdersBySellerId(@Param("sellerId") Long sellerId, 
                              @Param("excludedStatuses") List<OrderStatus> excludedStatuses);
  
  /**
   * Count shipped/delivered orders for a seller (fulfilled orders).
   */
  @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.items oi " +
         "WHERE oi.seller.id = :sellerId AND o.status IN :fulfilledStatuses")
  Long countFulfilledOrdersBySellerId(@Param("sellerId") Long sellerId,
                                       @Param("fulfilledStatuses") List<OrderStatus> fulfilledStatuses);
}
