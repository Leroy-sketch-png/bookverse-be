package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Moderation.Suspension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SuspensionRepository extends JpaRepository<Suspension, Long> {
    
    Page<Suspension> findByUserId(Long userId, Pageable pageable);
    
    Optional<Suspension> findByUserIdAndIsActiveTrue(Long userId);
    
    List<Suspension> findByIsActiveTrueAndEndsAtBefore(LocalDateTime now);
    
    long countByUserId(Long userId);
    
    boolean existsByUserIdAndIsActiveTrue(Long userId);
}
