package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Order_Payment.OrderTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderTimelineRepository extends JpaRepository<OrderTimeline, UUID> {
}
