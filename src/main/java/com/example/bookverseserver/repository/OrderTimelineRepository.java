package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.Order_Payment.OrderTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderTimelineRepository extends JpaRepository<OrderTimeline, Long> {
    
    /**
     * Find all timeline events for an order, sorted by creation time.
     */
    List<OrderTimeline> findByOrderOrderByCreatedAtAsc(Order order);
}
