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
}
