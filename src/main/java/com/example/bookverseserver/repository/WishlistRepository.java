package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Product.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    /**
     * Find wishlist items with fully loaded listing, bookMeta, seller, and photos.
     * Prevents LazyInitializationException when mapping to DTOs.
     */
    @Query("SELECT DISTINCT w FROM Wishlist w " +
           "LEFT JOIN FETCH w.listing l " +
           "LEFT JOIN FETCH l.seller s " +
           "LEFT JOIN FETCH s.userProfile " +
           "LEFT JOIN FETCH l.bookMeta bm " +
           "LEFT JOIN FETCH bm.images " +
           "LEFT JOIN FETCH l.photos " +
           "WHERE w.user.id = :userId")
    List<Wishlist> findByUserIdWithDetails(@Param("userId") Long userId);

    // Tìm wishlist theo user (có phân trang) - simple version
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