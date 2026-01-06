package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Moderation.Warning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WarningRepository extends JpaRepository<Warning, Long> {
    
    Page<Warning> findByUserId(Long userId, Pageable pageable);
    
    List<Warning> findByUserIdAndAcknowledgedFalse(Long userId);
    
    long countByUserId(Long userId);
    
    long countByUserIdAndExpiresAtAfter(Long userId, LocalDateTime now);
    
    List<Warning> findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(Long userId, LocalDateTime now);
}
