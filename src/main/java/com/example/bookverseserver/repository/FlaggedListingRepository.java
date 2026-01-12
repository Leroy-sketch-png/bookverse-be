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
    
    @Query("SELECT f FROM FlaggedListing f WHERE f.status = :status ORDER BY " +
           "CASE f.severity WHEN 'CRITICAL' THEN 0 WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 ELSE 3 END, " +
           "f.flaggedAt ASC")
    Page<FlaggedListing> findByStatusOrderedByPriority(@Param("status") FlagStatus status, Pageable pageable);
    
    boolean existsByListingIdAndStatusIn(Long listingId, Collection<FlagStatus> statuses);
    
    Optional<FlaggedListing> findByListingIdAndStatusIn(Long listingId, Collection<FlagStatus> statuses);
}
