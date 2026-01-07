package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Product.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // Tìm wishlist theo user (có phân trang)
    Page<Wishlist> findByUserId(Long userId, Pageable pageable);

    // Kiểm tra tồn tại
    boolean existsByUserIdAndListingId(Long userId, Long listingId);

    // Tìm cụ thể để xóa
    Optional<Wishlist> findByUserIdAndListingId(Long userId, Long listingId);

    void deleteByUserIdAndListingId(Long userId, Long listingId);

    Integer countByUserId(Long userId);
    
    // For seller analytics: count wishlist entries for seller's listings
    long countByListingIdIn(java.util.List<Long> listingIds);
}