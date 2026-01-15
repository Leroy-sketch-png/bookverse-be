package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Moderation.Dispute;
import com.example.bookverseserver.enums.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    
    Page<Dispute> findByStatus(DisputeStatus status, Pageable pageable);
    
    Page<Dispute> findByBuyerId(Long buyerId, Pageable pageable);
    
    Page<Dispute> findBySellerId(Long sellerId, Pageable pageable);
    
    Page<Dispute> findByAssignedToId(Long moderatorId, Pageable pageable);
    
    Optional<Dispute> findByOrderId(Long orderId);
    
    long countByStatus(DisputeStatus status);
    
    long countBySellerId(Long sellerId);
    
    /**
     * Count disputes with specific status created within a date range (for trend calculations).
     */
    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.status = :status AND d.createdAt >= :start AND d.createdAt < :end")
    long countByStatusAndCreatedAtBetween(
            @Param("status") DisputeStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
