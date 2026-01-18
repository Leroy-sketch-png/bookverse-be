package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Product.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for transaction-based reviews.
 * 
 * MARKETPLACE MODEL: Reviews are on ORDER ITEMS (verified purchases).
 * - One review per order item (unique constraint)
 * - Reviews build SELLER reputation, not book ratings
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // =========================================================================
    // Core Review Operations
    // =========================================================================

    /**
     * Check if an order item already has a review.
     */
    boolean existsByOrderItemId(Long orderItemId);

    /**
     * Find review by order item ID.
     */
    Optional<Review> findByOrderItemId(Long orderItemId);

    /**
     * Find review by ID and user (for owner check).
     */
    Optional<Review> findByIdAndUserId(Long id, Long userId);

    /**
     * Find reviews by user (paginated).
     */
    Page<Review> findByUserId(Long userId, Pageable pageable);

    // =========================================================================
    // Listing Reviews (for product page)
    // =========================================================================

    /**
     * Find visible reviews for a specific listing.
     */
    Page<Review> findByListingIdAndIsVisibleTrueAndIsHiddenFalse(Long listingId, Pageable pageable);

    /**
     * Find visible reviews for a listing filtered by rating.
     */
    Page<Review> findByListingIdAndRatingAndIsVisibleTrueAndIsHiddenFalse(
            Long listingId, Integer rating, Pageable pageable);

    /**
     * Count visible reviews for a listing.
     */
    long countByListingIdAndIsVisibleTrueAndIsHiddenFalse(Long listingId);

    /**
     * Get average rating for a listing.
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.listing.id = :listingId AND r.isVisible = true AND r.isHidden = false")
    Double findAverageRatingByListingId(@Param("listingId") Long listingId);

    /**
     * Get rating distribution for a listing.
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.listing.id = :listingId AND r.isVisible = true AND r.isHidden = false GROUP BY r.rating")
    List<Object[]> findRatingDistributionByListingId(@Param("listingId") Long listingId);

    // =========================================================================
    // Seller Reviews (for seller profile & reputation)
    // =========================================================================

    /**
     * Find visible reviews for a seller (across all their listings).
     */
    Page<Review> findBySellerIdAndIsVisibleTrueAndIsHiddenFalse(Long sellerId, Pageable pageable);

    /**
     * Find visible reviews for a seller filtered by rating.
     */
    Page<Review> findBySellerIdAndRatingAndIsVisibleTrueAndIsHiddenFalse(
            Long sellerId, Integer rating, Pageable pageable);

    /**
     * Count visible reviews for a seller.
     */
    long countBySellerIdAndIsVisibleTrueAndIsHiddenFalse(Long sellerId);

    /**
     * Get average rating for a seller (SELLER REPUTATION).
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.seller.id = :sellerId AND r.isVisible = true AND r.isHidden = false")
    Double calculateAverageRatingForSeller(@Param("sellerId") Long sellerId);

    /**
     * Get rating distribution for a seller.
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.seller.id = :sellerId AND r.isVisible = true AND r.isHidden = false GROUP BY r.rating")
    List<Object[]> getRatingDistributionForSeller(@Param("sellerId") Long sellerId);

    /**
     * Count reviews for a seller (for stats).
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.seller.id = :sellerId AND r.isVisible = true AND r.isHidden = false")
    Integer countByListingSellerId(@Param("sellerId") Long sellerId);

    /**
     * Find reviews for a seller with pagination (for public seller profile).
     */
    @Query("""
        SELECT r FROM Review r
        WHERE r.seller.id = :sellerId
        AND r.isVisible = true
        AND r.isHidden = false
        ORDER BY r.createdAt DESC
        """)
    List<Review> findByListingSellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    // =========================================================================
    // Admin/Moderation
    // =========================================================================

    /**
     * Find all reviews for a listing (including hidden, for admin).
     */
    Page<Review> findByListingId(Long listingId, Pageable pageable);

    /**
     * Find all reviews for a seller (including hidden, for admin).
     */
    Page<Review> findBySellerId(Long sellerId, Pageable pageable);

    /**
     * Find hidden/flagged reviews for moderation.
     */
    Page<Review> findByIsHiddenTrue(Pageable pageable);

    /**
     * Get global average rating across all visible reviews (for public stats).
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.isVisible = true AND r.isHidden = false")
    Double getAverageRating();
    
    // =========================================================================
    // AI Review Summarization
    // =========================================================================
    
    /**
     * Find reviews for a book (via listing's bookMeta) for AI summarization.
     */
    @Query("""
        SELECT r FROM Review r
        JOIN r.listing l
        WHERE l.bookMeta.id = :bookId
        AND r.isVisible = true
        AND r.isHidden = false
        ORDER BY r.createdAt DESC
        """)
    List<Review> findByBookMetaIdOrderByCreatedAtDesc(@Param("bookId") Long bookId);

    // =========================================================================
    // BOOK-LEVEL RATING AGGREGATION (REAL DATA, NOT FABRICATED)
    // =========================================================================

    /**
     * Compute REAL average rating for a book across ALL its listings.
     * This is the HONEST alternative to hardcoded book_meta.average_rating.
     */
    @Query("""
        SELECT AVG(r.rating) FROM Review r
        JOIN r.listing l
        WHERE l.bookMeta.id = :bookId
        AND r.isVisible = true
        AND r.isHidden = false
        """)
    Double calculateAverageRatingForBook(@Param("bookId") Long bookId);

    /**
     * Count REAL reviews for a book across ALL its listings.
     * This is the HONEST alternative to hardcoded book_meta.total_reviews.
     */
    @Query("""
        SELECT COUNT(r) FROM Review r
        JOIN r.listing l
        WHERE l.bookMeta.id = :bookId
        AND r.isVisible = true
        AND r.isHidden = false
        """)
    Long countReviewsForBook(@Param("bookId") Long bookId);
}
