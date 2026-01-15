package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Moderation.FlaggedListing;
import com.example.bookverseserver.enums.FlagSeverity;
import com.example.bookverseserver.enums.FlagStatus;
import com.example.bookverseserver.enums.FlagType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface FlaggedListingRepository extends JpaRepository<FlaggedListing, Long> {
    
    Page<FlaggedListing> findByStatus(FlagStatus status, Pageable pageable);
    
    Page<FlaggedListing> findBySeverity(FlagSeverity severity, Pageable pageable);
    
    Page<FlaggedListing> findByFlagType(FlagType flagType, Pageable pageable);
    
    Page<FlaggedListing> findByStatusAndSeverity(FlagStatus status, FlagSeverity severity, Pageable pageable);
    
    long countByStatus(FlagStatus status);
    
    long countBySeverity(FlagSeverity severity);
    
    /**
     * Count flags with specific status created within a date range (for trend calculations).
     */
    @Query("SELECT COUNT(f) FROM FlaggedListing f WHERE f.status = :status AND f.flaggedAt >= :start AND f.flaggedAt < :end")
    long countByStatusAndFlaggedAtBetween(
            @Param("status") FlagStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    
    @Query("SELECT f FROM FlaggedListing f " +
           "LEFT JOIN FETCH f.listing l " +
           "LEFT JOIN FETCH l.seller " +
           "LEFT JOIN FETCH l.bookMeta " +
           "LEFT JOIN FETCH l.photos " +
           "WHERE f.status = :status ORDER BY " +
           "CASE f.severity WHEN 'CRITICAL' THEN 0 WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 ELSE 3 END, " +
           "f.flaggedAt ASC")
    Page<FlaggedListing> findByStatusOrderedByPriority(@Param("status") FlagStatus status, Pageable pageable);
    
    @Query("SELECT f FROM FlaggedListing f " +
           "LEFT JOIN FETCH f.listing l " +
           "LEFT JOIN FETCH l.seller " +
           "LEFT JOIN FETCH l.bookMeta " +
           "LEFT JOIN FETCH l.photos")
    Page<FlaggedListing> findAllWithDetails(Pageable pageable);
    
    boolean existsByListingIdAndStatusIn(Long listingId, Collection<FlagStatus> statuses);
    
    Optional<FlaggedListing> findByListingIdAndStatusIn(Long listingId, Collection<FlagStatus> statuses);
}
