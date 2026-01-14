package com.example.bookverseserver.dto.response.Book;

import com.example.bookverseserver.dto.response.User.SellerProfileResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Complete book detail response with Open Library enrichment.
 * 
 * Includes rich discovery data for product pages:
 * - Basic: title, authors, publisher, pages
 * - Discovery: tags, categories, places, people, times
 * - Marketing: firstLine, externalLinks
 * - Cross-platform: openLibraryId, goodreadsId
 */
@Data
public class BookDetailResponse {
    private Long id;
    private String title;
    private String isbn;
    private String publisher;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date publicationDate;
    private Integer pageCount;
    private String language;
    private String description;
    private String coverImageUrl;
    private List<AuthorResponse> authors;
    private List<CategoryResponse> categories;
    
    // ═══════════════════════════════════════════════════════════════════════════
    // GRANULAR TAGS (NEW) - For filtering: Romance, Historical, Mystery, etc.
    // ═══════════════════════════════════════════════════════════════════════════
    private List<String> tags;
    
    // ═══════════════════════════════════════════════════════════════════════════
    // RICH DISCOVERY DATA (NEW)
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Famous opening line
     * e.g., "It is a truth universally acknowledged..."
     */
    private String firstLine;
    
    /**
     * Story locations: ["England", "Derbyshire"]
     */
    private List<String> subjectPlaces;
    
    /**
     * Characters: ["Elizabeth Bennet", "Mr. Darcy"]
     */
    private List<String> subjectPeople;
    
    /**
     * Time periods: ["19th century"]
     */
    private List<String> subjectTimes;
    
    /**
     * External links: Wikipedia, Britannica, etc.
     */
    private List<ExternalLinkResponse> externalLinks;
    
    /**
     * Open Library Edition ID
     */
    private String openLibraryId;
    
    /**
     * Goodreads ID for linking
     */
    private String goodreadsId;
    
    // ═══════════════════════════════════════════════════════════════════════════
    // MARKETPLACE: AVAILABLE LISTINGS
    // A book can have MANY listings from MANY sellers at different prices
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Price range across all active listings
     */
    private PriceRange priceRange;
    
    /**
     * All active listings for this book from different sellers
     */
    private List<ListingPreview> listings;
    
    /**
     * Total count of active listings
     */
    private Integer totalListings;
    
    // ═══════════════════════════════════════════════════════════════════════════
    // RATINGS (for the BOOK, not individual listings)
    // ═══════════════════════════════════════════════════════════════════════════
    private Double averageRating;
    private Integer totalReviews;
    
    @Data
    public static class ExternalLinkResponse {
        private String title;
        private String url;
    }
    
    @Data
    public static class PriceRange {
        private BigDecimal min;
        private BigDecimal max;
        private String currency;
        
        public PriceRange(BigDecimal min, BigDecimal max, String currency) {
            this.min = min;
            this.max = max;
            this.currency = currency;
        }
    }
    
    @Data
    public static class ListingPreview {
        private Long id;
        private SellerCompact seller;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private BigDecimal finalPrice;
        private String condition;
        private Integer quantity;
        private String createdAt;
    }
    
    @Data
    public static class SellerCompact {
        private Long id;
        private String name;
        private String avatar;
        private Boolean isPro;
        private Double rating;
    }
}