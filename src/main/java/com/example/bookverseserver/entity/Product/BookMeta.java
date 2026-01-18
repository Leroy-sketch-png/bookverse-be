package com.example.bookverseserver.entity.Product;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.*;

@Entity
@Table(name = "book_meta")
@Getter
@Setter
@ToString(exclude = {"images", "authors", "categories", "tags"})
@EqualsAndHashCode(exclude = {"images", "authors", "categories", "tags"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String title;

    @Column(unique = true)
    String isbn;

    @Column(columnDefinition = "TEXT")
    String description;

    String publisher;
    LocalDate publishedDate;
    Integer pages;
    BigDecimal listPrice;
    
    @Column(length = 10)
    @Builder.Default
    String language = "en";
    
    @Column(name = "average_rating", precision = 3, scale = 2)
    BigDecimal averageRating;
    
    @Column(name = "total_reviews")
    @Builder.Default
    Integer totalReviews = 0;

    // ═══════════════════════════════════════════════════════════════════════════
    // OPEN LIBRARY ENRICHMENT FIELDS (NEW)
    // These fields capture the FULL VALUE from Open Library API
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Famous opening line - marketing gold!
     * e.g., "It is a truth universally acknowledged, that a single man..."
     */
    @Column(name = "first_line", columnDefinition = "TEXT")
    String firstLine;
    
    /**
     * Story locations as JSON array: ["England", "Derbyshire", "Hertfordshire"]
     * Enables location-based discovery
     */
    @Column(name = "subject_places", columnDefinition = "TEXT")
    String subjectPlaces;
    
    /**
     * Character names as JSON array: ["Elizabeth Bennet", "Mr. Darcy"]
     * Enables character-based discovery
     */
    @Column(name = "subject_people", columnDefinition = "TEXT")
    String subjectPeople;
    
    /**
     * Time periods as JSON array: ["19th century", "1789-1820"]
     * Enables era-based discovery
     */
    @Column(name = "subject_times", columnDefinition = "TEXT")
    String subjectTimes;
    
    /**
     * External links as JSON: [{"title": "Wikipedia", "url": "..."}]
     * Provides additional context
     */
    @Column(name = "external_links", columnDefinition = "TEXT")
    String externalLinks;
    
    /**
     * Table of contents as JSON: [{"label": "Chapter 1", "title": "The boy who lived"}, ...]
     * Enables book preview feature for buyers
     */
    @Column(name = "table_of_contents", columnDefinition = "TEXT")
    String tableOfContents;
    
    /**
     * Open Library Work/Edition ID: "OL8479867M"
     * For attribution and future data syncs
     */
    @Column(name = "openlibrary_id", length = 50)
    String openLibraryId;
    
    /**
     * Goodreads ID for cross-platform linking
     */
    @Column(name = "goodreads_id", length = 50)
    String goodreadsId;
    
    /**
     * Google Books ID for cross-platform linking
     */
    @Column(name = "google_books_id", length = 50)
    String googleBooksId;

    // ═══════════════════════════════════════════════════════════════════════════

    LocalDateTime deletedAt;
    Long deletedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    // One-to-Many relationship for images
    // Changed to Set to avoid MultipleBagFetchException when fetching with photos
    // @BatchSize prevents N+1 queries when accessing images in Specification-based queries
    @OneToMany(mappedBy = "bookMeta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @org.hibernate.annotations.BatchSize(size = 50)
    Set<BookImage> images = new HashSet<>();

    // Many-to-Many relationship for authors
    @ManyToMany(fetch = FetchType.LAZY)
    @org.hibernate.annotations.BatchSize(size = 50)
    @JoinTable(
            name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    @JsonManagedReference
    Set<Author> authors = new HashSet<>();

    // Many-to-Many relationship for categories (broad: Fiction, Science, etc.)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_category",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    Set<Category> categories = new HashSet<>();
    
    // Many-to-Many relationship for tags (granular: romance, mystery, regency, etc.)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_tag_mapping",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    Set<BookTag> tags = new HashSet<>();

    public String getCoverImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.stream().findFirst().map(BookImage::getUrl).orElse(null);
        }
        return null;
    }
}