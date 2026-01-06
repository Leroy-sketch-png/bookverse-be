package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Order_Payment.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * Find all order items for given listing IDs (for seller dashboard).
     */
    List<OrderItem> findByListingIdIn(List<Long> listingIds);
}
