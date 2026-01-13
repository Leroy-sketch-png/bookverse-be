package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Moderation.UserReport;
import com.example.bookverseserver.enums.ReportPriority;
import com.example.bookverseserver.enums.ReportStatus;
import com.example.bookverseserver.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    
    Page<UserReport> findByStatus(ReportStatus status, Pageable pageable);
    
    Page<UserReport> findByReportType(ReportType type, Pageable pageable);
    
    Page<UserReport> findByPriority(ReportPriority priority, Pageable pageable);
    
    Page<UserReport> findByStatusAndReportType(ReportStatus status, ReportType type, Pageable pageable);
    
    Page<UserReport> findByReporterId(Long reporterId, Pageable pageable);
    
    Page<UserReport> findByReportedUserId(Long userId, Pageable pageable);
    
    Page<UserReport> findByAssignedToId(Long moderatorId, Pageable pageable);
    
    long countByStatus(ReportStatus status);
    
    long countByPriority(ReportPriority priority);
    
    long countByReportedUserId(Long userId);
    
    long countByReporterId(Long reporterId);
    
    @Query("SELECT r FROM UserReport r " +
           "LEFT JOIN FETCH r.reporter " +
           "LEFT JOIN FETCH r.reportedUser " +
           "LEFT JOIN FETCH r.relatedOrder " +
           "WHERE r.status = :status ORDER BY " +
           "CASE r.priority WHEN 'CRITICAL' THEN 0 WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 ELSE 3 END, " +
           "r.createdAt ASC")
    Page<UserReport> findByStatusOrderedByPriority(@Param("status") ReportStatus status, Pageable pageable);
    
    @Query("SELECT r FROM UserReport r " +
           "LEFT JOIN FETCH r.reporter " +
           "LEFT JOIN FETCH r.reportedUser " +
           "LEFT JOIN FETCH r.relatedOrder")
    Page<UserReport> findAllWithDetails(Pageable pageable);
}
