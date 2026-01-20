package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.User.ProSellerApplication;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProSellerApplicationRepository extends JpaRepository<ProSellerApplication, Long> {
    
    /**
     * Find the most recent application by user.
     */
    Optional<ProSellerApplication> findFirstByUserOrderBySubmittedAtDesc(User user);
    
    /**
     * Check if user has a pending application.
     */
    boolean existsByUserAndStatus(User user, ApplicationStatus status);
    
    /**
     * Find all applications by status (for admin review).
     */
    Page<ProSellerApplication> findByStatus(ApplicationStatus status, Pageable pageable);
    
    /**
     * Find all applications by status with eager-loaded User (for admin review).
     * Prevents LazyInitializationException when accessing User.username/email.
     */
    @Query("SELECT p FROM ProSellerApplication p " +
           "LEFT JOIN FETCH p.user " +
           "WHERE p.status = :status")
    Page<ProSellerApplication> findByStatusWithUser(@Param("status") ApplicationStatus status, Pageable pageable);
    
    /**
     * Find all applications (for admin dashboard).
     */
    Page<ProSellerApplication> findAllByOrderBySubmittedAtDesc(Pageable pageable);
    
    /**
     * Find all applications with eager-loaded User (for admin dashboard).
     * Prevents LazyInitializationException when accessing User.username/email.
     */
    @Query("SELECT p FROM ProSellerApplication p " +
           "LEFT JOIN FETCH p.user " +
           "ORDER BY p.submittedAt DESC")
    Page<ProSellerApplication> findAllWithUserOrderBySubmittedAtDesc(Pageable pageable);
}
