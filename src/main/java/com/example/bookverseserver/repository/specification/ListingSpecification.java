package com.example.bookverseserver.repository.specification;

import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.enums.ListingStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for dynamic Listing queries.
 * Enables flexible filtering by seller, book, status, and visibility.
 */
public class ListingSpecification {

    private ListingSpecification() {
        // Utility class - prevent instantiation
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
