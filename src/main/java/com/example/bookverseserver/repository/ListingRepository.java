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

    @NotNull
    @Query("""
                SELECT l
                FROM Listing l
                LEFT JOIN FETCH l.bookMeta bm
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
     * Count all listings by seller ID.
     */
    long countBySellerId(Long sellerId);

    // ============ OPTIMIZED QUERIES (N+1 Prevention) ============

    /**
     * Find all active, visible listings with ALL relations eagerly fetched.
     * This prevents N+1 queries when mapping to DTOs.
     * 
     * Relations fetched:
     * - bookMeta (with authors, images)
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
     * Find seller listings by status and category with pagination.
     */
    @Query("""
            SELECT l FROM Listing l
            LEFT JOIN FETCH l.bookMeta bm
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
}
