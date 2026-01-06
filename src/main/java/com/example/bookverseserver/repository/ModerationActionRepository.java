package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Moderation.ModerationAction;
import com.example.bookverseserver.enums.ModerationActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ModerationActionRepository extends JpaRepository<ModerationAction, Long> {
    
    Page<ModerationAction> findByModeratorId(Long moderatorId, Pageable pageable);
    
    Page<ModerationAction> findByAffectedUserId(Long userId, Pageable pageable);
    
    Page<ModerationAction> findByActionType(ModerationActionType actionType, Pageable pageable);
    
    Page<ModerationAction> findByTargetTypeAndTargetId(String targetType, Long targetId, Pageable pageable);
    
    List<ModerationAction> findByTargetTypeAndTargetId(String targetType, Long targetId);
    
    long countByModeratorIdAndCreatedAtBetween(Long moderatorId, LocalDateTime start, LocalDateTime end);
}
