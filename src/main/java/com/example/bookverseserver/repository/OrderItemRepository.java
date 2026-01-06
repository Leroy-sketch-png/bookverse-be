package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.Order_Payment.OrderItem;
import com.example.bookverseserver.entity.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
