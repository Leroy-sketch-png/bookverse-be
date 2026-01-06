package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Product.Promotion;
import com.example.bookverseserver.enums.PromotionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    
    /**
     * Find all promotions by seller.
     */
    List<Promotion> findBySellerId(Long sellerId);
    
    /**
     * Find promotions by seller with pagination.
     */
    Page<Promotion> findBySellerId(Long sellerId, Pageable pageable);
    
    /**
     * Find promotions by seller and status.
     */
    List<Promotion> findBySellerIdAndStatus(Long sellerId, PromotionStatus status);
    
    /**
     * Find active promotions for seller.
     */
    List<Promotion> findBySellerIdAndStatusAndStartDateBeforeAndEndDateAfter(
            Long sellerId, 
            PromotionStatus status, 
            LocalDateTime now1, 
            LocalDateTime now2);
    
    /**
     * Count promotions by seller and status.
     */
    long countBySellerIdAndStatus(Long sellerId, PromotionStatus status);
}
