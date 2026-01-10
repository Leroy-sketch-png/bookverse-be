package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.Order_Payment.OrderItem;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * Find all order items for given listing IDs (for seller dashboard).
     */
    List<OrderItem> findByListingIdIn(List<Long> listingIds);
    
    /**
     * Check if order contains any items from this seller.
     * Used for seller order status update validation.
     */
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi WHERE oi.order = :order AND oi.seller = :seller")
    boolean existsByOrderAndSeller(@Param("order") Order order, @Param("seller") User seller);
    
    /**
     * Find all items in an order that belong to a specific seller.
     */
    List<OrderItem> findByOrderAndSeller(Order order, User seller);
    
    /**
     * Verify purchase for review: Check if user bought this listing in a DELIVERED order.
     * Returns the order item if found.
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId " +
           "AND oi.listing.id = :listingId " +
           "AND oi.order.user.id = :userId " +
           "AND oi.order.status = :status")
    Optional<OrderItem> findByOrderIdAndListingIdAndUserIdAndOrderStatus(
            @Param("orderId") Long orderId,
            @Param("listingId") Long listingId,
            @Param("userId") Long userId,
            @Param("status") OrderStatus status);

    /**
     * Find all order items for a seller (for payout calculation).
     */
    List<OrderItem> findBySeller(User seller);
    
    /**
     * Count unique buyers for a seller (for repeat buyer rate calculation).
     */
    @Query("SELECT COUNT(DISTINCT oi.order.user.id) FROM OrderItem oi WHERE oi.seller.id = :sellerId")
    long countDistinctBuyersBySellerId(@Param("sellerId") Long sellerId);
    
    /**
     * Count repeat buyers for a seller (buyers who ordered more than once).
     */
    @Query("SELECT COUNT(*) FROM (" +
           "SELECT oi.order.user.id AS userId FROM OrderItem oi " +
           "WHERE oi.seller.id = :sellerId " +
           "GROUP BY oi.order.user.id " +
           "HAVING COUNT(DISTINCT oi.order.id) > 1) AS repeatBuyers")
    long countRepeatBuyersBySellerId(@Param("sellerId") Long sellerId);
}
