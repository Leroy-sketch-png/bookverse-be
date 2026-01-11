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
