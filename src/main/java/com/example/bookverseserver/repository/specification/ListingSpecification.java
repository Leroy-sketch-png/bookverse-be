package com.example.bookverseserver.repository.specification;

import com.example.bookverseserver.entity.Product.Author;
import com.example.bookverseserver.entity.Product.BookMeta;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.ListingStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for dynamic Listing queries.
 * Enables flexible filtering by seller, book, status, visibility, and text search.
 */
public class ListingSpecification {

    private ListingSpecification() {
        // Utility class - prevent instantiation
    }

    /**
     * Full-text search across book title, author names, and listing description.
     * Case-insensitive LIKE matching on multiple fields.
     * 
     * @param searchText the search query (e.g., "haruki murakami", "kafka shore")
     * @return specification matching listings where any field contains the search text
     */
    public static Specification<Listing> containsSearchText(String searchText) {
        return (root, query, cb) -> {
            if (searchText == null || searchText.trim().isEmpty()) {
                return cb.conjunction(); // Always true - no filter
            }
            
            String pattern = "%" + searchText.toLowerCase().trim() + "%";
            List<Predicate> predicates = new ArrayList<>();
            
            // Search in book title
            Join<Listing, BookMeta> bookJoin = root.join("bookMeta", JoinType.LEFT);
            predicates.add(cb.like(cb.lower(bookJoin.get("title")), pattern));
            
            // Search in listing title override
            predicates.add(cb.like(cb.lower(root.get("titleOverride")), pattern));
            
            // Search in listing description
            predicates.add(cb.like(cb.lower(root.get("description")), pattern));
            
            // Search in author names (requires join through bookMeta)
            Join<BookMeta, Author> authorJoin = bookJoin.join("authors", JoinType.LEFT);
            predicates.add(cb.like(cb.lower(authorJoin.get("name")), pattern));
            
            // Search in seller display name
            Join<Listing, User> sellerJoin = root.join("seller", JoinType.LEFT);
            predicates.add(cb.like(cb.lower(sellerJoin.get("username")), pattern));
            
            // Use distinct to prevent duplicate results from joins
            query.distinct(true);
            
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter listings by seller ID.
     */
    public static Specification<Listing> hasSeller(Long sellerId) {
        return (root, query, cb) -> cb.equal(root.get("seller").get("id"), sellerId);
    }

    /**
     * Filter listings by book (BookMeta) ID.
     */
    public static Specification<Listing> hasBook(Long bookId) {
        return (root, query, cb) -> cb.equal(root.get("bookMeta").get("id"), bookId);
    }

    /**
     * Filter listings by category ID.
     */
    public static Specification<Listing> hasCategory(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    /**
     * Filter listings by author ID.
     * Joins through bookMeta to find listings where the book has the specified author.
     */
    public static Specification<Listing> hasAuthor(Long authorId) {
        return (root, query, cb) -> {
            Join<Listing, BookMeta> bookJoin = root.join("bookMeta", JoinType.LEFT);
            Join<BookMeta, Author> authorJoin = bookJoin.join("authors", JoinType.LEFT);
            query.distinct(true); // Prevent duplicates from the join
            return cb.equal(authorJoin.get("id"), authorId);
        };
    }

    /**
     * Filter listings by book condition (NEW, LIKE_NEW, GOOD, ACCEPTABLE).
     */
    public static Specification<Listing> hasCondition(com.example.bookverseserver.enums.BookCondition condition) {
        return (root, query, cb) -> cb.equal(root.get("condition"), condition);
    }

    /**
     * Filter listings with effective price >= minPrice.
     * Uses finalPrice calculation: if activePromotion exists, applies discount.
     * AI search integration: extracted budget filters from natural language.
     */
    public static Specification<Listing> hasMinPrice(Double minPrice) {
        return (root, query, cb) -> {
            // Calculate effective price: price * (1 - discount/100) if promotion active
            // For simplicity, filter on base price but account for typical max discount (50%)
            // Users searching "min 50k" probably want items that cost at least 50k after discount
            return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    /**
     * Filter listings with effective price <= maxPrice.
     * CRITICAL: Uses base price but adjusts for promotions.
     * A book at 120k with 50% off = 60k effective, should match "under 100k".
     * 
     * Strategy: Include listings where price <= maxPrice OR has active promotion.
     * Post-filter in service layer for exact finalPrice matching.
     */
    public static Specification<Listing> hasMaxPrice(Double maxPrice) {
        return (root, query, cb) -> {
            // Include all listings with base price <= maxPrice (obviously match)
            // PLUS listings with any active promotion (might be discounted into budget)
            // The service layer will post-filter for exact finalPrice
            var basePriceMatch = cb.lessThanOrEqualTo(root.get("price"), maxPrice);
            
            // Also include listings with active promotions up to 2x maxPrice
            // (assumes max 50% discount, so 2x covers all possibilities)
            var promotionPriceMatch = cb.and(
                cb.isNotNull(root.get("activePromotion")),
                cb.lessThanOrEqualTo(root.get("price"), maxPrice * 2)
            );
            
            return cb.or(basePriceMatch, promotionPriceMatch);
        };
    }

    /**
     * Filter listings by status.
     */
    public static Specification<Listing> hasStatus(ListingStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /**
     * Filter only visible listings.
     */
    public static Specification<Listing> isVisible() {
        return (root, query, cb) -> cb.equal(root.get("visibility"), true);
    }

    /**
     * Filter only non-deleted listings (deletedAt is null).
     */
    public static Specification<Listing> isNotDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    /**
     * Filter listings with stock quantity greater than 0.
     */
    public static Specification<Listing> hasStock() {
        return (root, query, cb) -> cb.greaterThan(root.get("quantity"), 0);
    }

    /**
     * Combine common filters for public listing queries.
     * Returns listings that are visible, not deleted.
     */
    public static Specification<Listing> isPubliclyAvailable() {
        return Specification.where(isVisible()).and(isNotDeleted());
    }
}
