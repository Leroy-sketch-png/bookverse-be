package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.enums.ListingStatus;
import com.google.firebase.database.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {

    List<Listing> findByBookMetaAndStatusAndVisibility(
            com.example.bookverseserver.entity.Product.BookMeta bookMeta,
            ListingStatus status,
            boolean visibility);

    /**
     * Batch fetch listing aggregations (count, min/max price) for multiple book IDs.
     * Returns Object[]: [bookMetaId, count, minPrice, maxPrice, currency]
     * Single query replaces N+1 individual queries.
     */
    @Query("""
            SELECT l.bookMeta.id, COUNT(l), MIN(l.price), MAX(l.price), MIN(l.currency)
            FROM Listing l
            WHERE l.bookMeta.id IN :bookIds
            AND l.status = 'ACTIVE'
            AND l.visibility = true
            AND l.deletedAt IS NULL
            GROUP BY l.bookMeta.id
            """)
    List<Object[]> findListingAggregationsByBookIds(@Param("bookIds") List<Long> bookIds);

    @NotNull
    @Query("""
                SELECT l
                FROM Listing l
                LEFT JOIN FETCH l.bookMeta bm
                LEFT JOIN FETCH bm.images
                LEFT JOIN FETCH l.seller s
                LEFT JOIN FETCH l.photos p
                WHERE l.id = :id
            """)
    Optional<Listing> findById(@NotNull @Param("id") Long id);

    /**
     * Atomically increment view count and update lastViewedAt timestamp.
     * 
     * @param id the listing ID
     */
    @Modifying
    @Query("UPDATE Listing l SET l.views = l.views + 1, l.lastViewedAt = CURRENT_TIMESTAMP WHERE l.id = :id")
    void incrementViewCount(@Param("id") Long id);

    /**
     * Atomically reserve stock for an order.
     * Returns the number of rows affected (1 if successful, 0 if insufficient stock).
     * Uses WHERE clause to prevent overselling - only succeeds if quantity >= requested amount.
     * 
     * @param id the listing ID
     * @param quantity the quantity to reserve (decrement)
     * @return 1 if successful, 0 if insufficient stock
     */
    @Modifying
    @Query("UPDATE Listing l SET l.quantity = l.quantity - :quantity, l.soldCount = l.soldCount + :quantity WHERE l.id = :id AND l.quantity >= :quantity")
    int reserveStock(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * Atomically restore stock after order cancellation or failure.
     * 
     * @param id the listing ID
     * @param quantity the quantity to restore (increment)
     */
    @Modifying
    @Query("UPDATE Listing l SET l.quantity = l.quantity + :quantity, l.soldCount = l.soldCount - :quantity WHERE l.id = :id")
    void restoreStock(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * Find related listings for the same book from different sellers.
     * Returns only active, visible listings, excluding the current listing.
     * Ordered by price ascending to show best deals first.
     * 
     * @param bookId           the book (BookMeta) ID
     * @param excludeListingId the listing ID to exclude (current listing)
     * @param pageable         pagination parameters
     * @return list of related listings
     */
    @Query("""
                SELECT l FROM Listing l
                LEFT JOIN FETCH l.seller s
                WHERE l.bookMeta.id = :bookId
                AND l.id != :excludeListingId
                AND l.status = 'ACTIVE'
                AND l.visibility = true
                AND l.deletedAt IS NULL
                ORDER BY l.price ASC
            """)
    List<Listing> findRelatedListings(
            @Param("bookId") Long bookId,
            @Param("excludeListingId") Long excludeListingId,
            Pageable pageable);

    /**
     * Find all listings by seller ID with pagination.
     */
    Page<Listing> findBySellerIdAndDeletedAtIsNull(Long sellerId, Pageable pageable);

    /**
     * Count listings by seller and status.
     */
    long countBySellerIdAndStatus(Long sellerId, ListingStatus status);

    /**
     * Find all listings by category with pagination.
     * Only return active, visible listings.
     */
    @Query("""
            SELECT l FROM Listing l
            LEFT JOIN FETCH l.bookMeta bm
            LEFT JOIN FETCH bm.images
            LEFT JOIN FETCH l.seller s
            LEFT JOIN FETCH l.category c
            WHERE l.category.id = :categoryId
            AND l.status = 'ACTIVE'
            AND l.visibility = true
            AND l.deletedAt IS NULL
        """)
    Page<Listing> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * Find all listings by category slug with pagination.
     */
    @Query("""
            SELECT l FROM Listing l
            LEFT JOIN FETCH l.bookMeta bm
            LEFT JOIN FETCH bm.images
            LEFT JOIN FETCH l.seller s
            LEFT JOIN FETCH l.category c
            WHERE l.category.slug = :categorySlug
            AND l.status = 'ACTIVE'
            AND l.visibility = true
            AND l.deletedAt IS NULL
        """)
    Page<Listing> findByCategorySlug(@Param("categorySlug") String categorySlug, Pageable pageable);

    /**
     * Find all listings by seller ID (for seller dashboard).
     */
    List<Listing> findBySellerId(Long sellerId);

    /**
     * Find seller listings by status with pagination.
     */
    Page<Listing> findBySellerIdAndStatus(Long sellerId, ListingStatus status, Pageable pageable);

    /**
     * Find seller listings excluding a specific status with pagination.
     */
    Page<Listing> findBySellerIdAndStatusNot(Long sellerId, ListingStatus status, Pageable pageable);

    /**
     * Count listings by status (for admin stats).
     */
    long countByStatus(ListingStatus status);

    /**
     * Count listings with specific status created within a date range (for trend calculations).
     */
    @Query("SELECT COUNT(l) FROM Listing l WHERE l.status = :status AND l.createdAt >= :start AND l.createdAt < :end")
    long countByStatusAndCreatedAtBetween(
            @Param("status") ListingStatus status,
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);

    /**
     * Count all listings by seller ID.
     */
    long countBySellerId(Long sellerId);

    /**
     * Get top category names for a seller based on their listing count.
     * Returns category names ordered by frequency (most common first).
     * Only counts active listings with a category assigned.
     * 
     * @param sellerId the seller's user ID
     * @return list of category names (most popular first)
     */
    @Query("""
            SELECT c.name FROM Listing l
            JOIN l.category c
            WHERE l.seller.id = :sellerId
            AND l.status = 'ACTIVE'
            AND l.deletedAt IS NULL
            GROUP BY c.id, c.name
            ORDER BY COUNT(l) DESC
            """)
    List<String> findTopCategoryNamesBySellerId(@Param("sellerId") Long sellerId);

    // ============ OPTIMIZED QUERIES (N+1 Prevention) ============

    /**
     * Find all active, visible listings with ALL relations eagerly fetched.
     * This prevents N+1 queries when mapping to DTOs.
     * 
     * Relations fetched:
     * - bookMeta (with authors, images, categories)
     * - seller (with userProfile)
     * - category
     * - photos
     * 
     * NOTE: Cannot use Specification with this method due to JOIN FETCH + pagination limitations.
     * Use for simple paginated listing queries without complex filters.
     */
    @Query(value = """
            SELECT DISTINCT l FROM Listing l
            LEFT JOIN FETCH l.bookMeta bm
            LEFT JOIN FETCH bm.authors
            LEFT JOIN FETCH bm.images
            LEFT JOIN FETCH bm.categories
            LEFT JOIN FETCH l.seller s
            LEFT JOIN FETCH s.userProfile
            LEFT JOIN FETCH l.category c
            LEFT JOIN FETCH l.photos p
            WHERE l.deletedAt IS NULL
            AND l.visibility = true
            AND (:status IS NULL OR l.status = :status)
            ORDER BY l.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT l) FROM Listing l
            WHERE l.deletedAt IS NULL
            AND l.visibility = true
            AND (:status IS NULL OR l.status = :status)
            """)
    Page<Listing> findAllWithDetails(@Param("status") ListingStatus status, Pageable pageable);

    /**
     * Find active listings sorted by soldCount (popular) with eager fetching.
     */
    @Query(value = """
            SELECT DISTINCT l FROM Listing l
            LEFT JOIN FETCH l.bookMeta bm
            LEFT JOIN FETCH bm.authors
            LEFT JOIN FETCH bm.images
            LEFT JOIN FETCH bm.categories
            LEFT JOIN FETCH l.seller s
            LEFT JOIN FETCH s.userProfile
            LEFT JOIN FETCH l.category c
            LEFT JOIN FETCH l.photos p
            WHERE l.deletedAt IS NULL
            AND l.visibility = true
            AND l.status = 'ACTIVE'
            ORDER BY l.soldCount DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT l) FROM Listing l
            WHERE l.deletedAt IS NULL
            AND l.visibility = true
            AND l.status = 'ACTIVE'
            """)
    Page<Listing> findPopularWithDetails(Pageable pageable);

    /**
     * Find active listings sorted by createdAt (new arrivals) with eager fetching.
     */
    @Query(value = """
            SELECT DISTINCT l FROM Listing l
            LEFT JOIN FETCH l.bookMeta bm
            LEFT JOIN FETCH bm.authors
            LEFT JOIN FETCH bm.images
            LEFT JOIN FETCH bm.categories
            LEFT JOIN FETCH l.seller s
            LEFT JOIN FETCH s.userProfile
            LEFT JOIN FETCH l.category c
            LEFT JOIN FETCH l.photos p
            WHERE l.deletedAt IS NULL
            AND l.visibility = true
            AND l.status = 'ACTIVE'
            ORDER BY l.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT l) FROM Listing l
            WHERE l.deletedAt IS NULL
            AND l.visibility = true
            AND l.status = 'ACTIVE'
            """)
    Page<Listing> findNewArrivalsWithDetails(Pageable pageable);

    /**
     * Find active listings sorted by views (trending) with eager fetching.
     * Used for "Trending Books" section on homepage.
     */
    @Query(value = """
            SELECT DISTINCT l FROM Listing l
            LEFT JOIN FETCH l.bookMeta bm
            LEFT JOIN FETCH bm.authors
            LEFT JOIN FETCH bm.images
            LEFT JOIN FETCH bm.categories
            LEFT JOIN FETCH l.seller s
            LEFT JOIN FETCH s.userProfile
            LEFT JOIN FETCH l.category c
            LEFT JOIN FETCH l.photos p
            WHERE l.deletedAt IS NULL
            AND l.visibility = true
            AND l.status = 'ACTIVE'
            ORDER BY l.views DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT l) FROM Listing l
            WHERE l.deletedAt IS NULL
            AND l.visibility = true
            AND l.status = 'ACTIVE'
            """)
    Page<Listing> findTrendingWithDetails(Pageable pageable);

    /**
     * Find active listings sorted by book's publishedDate (new releases) with eager fetching.
     * Used for "New Releases" section - shows books with most recent publication dates.
     */
    @Query(value = """
            SELECT DISTINCT l FROM Listing l
            LEFT JOIN FETCH l.bookMeta bm
            LEFT JOIN FETCH bm.authors
            LEFT JOIN FETCH bm.images
            LEFT JOIN FETCH bm.categories
            LEFT JOIN FETCH l.seller s
            LEFT JOIN FETCH s.userProfile
            LEFT JOIN FETCH l.category c
            LEFT JOIN FETCH l.photos p
            WHERE l.deletedAt IS NULL
            AND l.visibility = true
            AND l.status = 'ACTIVE'
            AND bm.publishedDate IS NOT NULL
            ORDER BY bm.publishedDate DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT l) FROM Listing l
            JOIN l.bookMeta bm
            WHERE l.deletedAt IS NULL
            AND l.visibility = true
            AND l.status = 'ACTIVE'
            AND bm.publishedDate IS NOT NULL
            """)
    Page<Listing> findNewReleasesByPublishedDateWithDetails(Pageable pageable);

    /**
     * Find seller listings by status and category with pagination.
     */
    @Query("""
            SELECT l FROM Listing l
            LEFT JOIN FETCH l.bookMeta bm
            LEFT JOIN FETCH bm.images
            LEFT JOIN FETCH l.category c
            LEFT JOIN FETCH l.photos p
            WHERE l.seller.id = :sellerId
            AND l.status = :status
            AND l.category.slug = :categorySlug
            AND l.deletedAt IS NULL
        """)
    List<Listing> findBySellerIdAndStatusAndCategorySlug(
            @Param("sellerId") Long sellerId, 
            @Param("status") ListingStatus status, 
            @Param("categorySlug") String categorySlug, 
            Pageable pageable);

    /**
     * Count new listings created after a date (for public stats).
     */
    long countByCreatedAtAfterAndStatus(java.time.LocalDateTime date, ListingStatus status);
    
    // ============ AI RECOMMENDATION QUERIES ============
    
    /**
     * Find recommendation candidates based on user preferences.
     * Filters by category, price, and condition.
     */
    @Query("""
            SELECT DISTINCT l FROM Listing l
            LEFT JOIN FETCH l.bookMeta bm
            LEFT JOIN FETCH bm.categories c
            LEFT JOIN FETCH bm.images
            LEFT JOIN FETCH l.seller s
            LEFT JOIN FETCH l.photos p
            WHERE l.status = :status
            AND l.visibility = true
            AND l.deletedAt IS NULL
            AND l.id NOT IN :excludeIds
            AND (:categories IS NULL OR c.slug IN :categories)
            AND (:maxPrice IS NULL OR l.price <= :maxPrice)
            AND l.condition IN :conditions
            ORDER BY l.soldCount DESC, l.views DESC
            """)
    List<Listing> findRecommendationCandidates(
            @Param("categories") List<String> categories,
            @Param("maxPrice") Double maxPrice,
            @Param("conditions") List<String> conditions,
            @Param("excludeIds") List<Long> excludeIds,
            @Param("status") ListingStatus status,
            Pageable pageable
    );
    
    /**
     * Find popular listings for fallback recommendations.
     */
    @Query("""
            SELECT DISTINCT l FROM Listing l
            LEFT JOIN FETCH l.bookMeta bm
            LEFT JOIN FETCH bm.images
            LEFT JOIN FETCH l.seller s
            LEFT JOIN FETCH l.photos p
            WHERE l.status = :status
            AND l.visibility = true
            AND l.deletedAt IS NULL
            AND l.id NOT IN :excludeIds
            ORDER BY l.soldCount DESC, l.views DESC
            """)
    List<Listing> findPopularListings(
            @Param("status") ListingStatus status,
            @Param("excludeIds") List<Long> excludeIds,
            Pageable pageable
    );
    
    // ============ ON SALE / PROMOTIONS QUERIES ============
    
    /**
     * Find active listings that have an active promotion (for "On Sale" section).
     * Only returns listings with ACTIVE status and a linked promotion that is ACTIVE.
     */
    @Query(value = """
            SELECT DISTINCT l FROM Listing l
            LEFT JOIN FETCH l.bookMeta bm
            LEFT JOIN FETCH bm.authors
            LEFT JOIN FETCH bm.images
            LEFT JOIN FETCH bm.categories
            LEFT JOIN FETCH l.seller s
            LEFT JOIN FETCH s.userProfile
            LEFT JOIN FETCH l.category c
            LEFT JOIN FETCH l.photos p
            LEFT JOIN FETCH l.activePromotion ap
            WHERE l.deletedAt IS NULL
            AND l.visibility = true
            AND l.status = 'ACTIVE'
            AND l.activePromotion IS NOT NULL
            AND l.activePromotion.status = 'ACTIVE'
            ORDER BY ap.discountPercentage DESC, l.soldCount DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT l) FROM Listing l
            WHERE l.deletedAt IS NULL
            AND l.visibility = true
            AND l.status = 'ACTIVE'
            AND l.activePromotion IS NOT NULL
            AND l.activePromotion.status = 'ACTIVE'
            """)
    Page<Listing> findOnSaleWithDetails(Pageable pageable);
}
