
package com.example.bookverseserver.dto.response.External;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Complete book data extracted from Open Library.
 * 
 * This captures the FULL VALUE — not just metadata, but discovery-enabling data:
 * - Basic: title, authors, pages, publisher
 * - Rich: tags (genres), places, people, times
 * - Marketing: firstLine, externalLinks
 * - Cross-platform: openLibraryId, goodreadsId
 */
@Data
@Builder(toBuilder = true)
public class RichBookData {
    // ═══════════════════════════════════════════════════════════════════════════
    // BASIC METADATA
    // ═══════════════════════════════════════════════════════════════════════════
    private String title;
    private String isbn;
    private String description;
    private String publisher;
    private String publishedDate;
    private int numberOfPages;
    private String coverUrl;
    
    // ═══════════════════════════════════════════════════════════════════════════
    // AUTHORS
    // ═══════════════════════════════════════════════════════════════════════════
    private List<String> authors;
    private List<String> authorKeys;  // /authors/OL21594A format
    
    // ═══════════════════════════════════════════════════════════════════════════
    // CATEGORIES & TAGS
    // ═══════════════════════════════════════════════════════════════════════════
    /**
     * Raw subject strings from Open Library (used for category mapping)
     */
    private List<String> categories;
    
    /**
     * Extracted genre tags: ["Romance", "Historical Fiction", "Regency"]
     */
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
     * Story locations: ["England", "Derbyshire", "Hertfordshire"]
     */
    private List<String> subjectPlaces;
    
    /**
     * Characters: ["Elizabeth Bennet", "Mr. Darcy"]
     */
    private List<String> subjectPeople;
    
    /**
     * Time periods: ["19th century", "1789-1820"]
     */
    private List<String> subjectTimes;
    
    /**
     * External links: [{"title": "Wikipedia", "url": "..."}]
     */
    private List<ExternalLink> externalLinks;
    
    /**
     * Table of contents - chapter titles for book preview
     * [{"label": "Chapter 1", "title": "The boy who lived"}, ...]
     */
    private List<TableOfContentsEntry> tableOfContents;
    
    // ═══════════════════════════════════════════════════════════════════════════
    // CROSS-PLATFORM IDs
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Open Library Edition/Work ID: "OL8479867M"
     */
    private String openLibraryId;
    
    /**
     * Goodreads ID for linking
     */
    private String goodreadsId;
    
    /**
     * Google Books ID for cross-platform linking
     */
    private String googleBooksId;
    
    @Data
    @Builder
    public static class ExternalLink {
        private String title;
        private String url;
    }
    
    @Data
    @Builder
    public static class TableOfContentsEntry {
        private String label;   // e.g., "Chapter 1"
        private String title;   // e.g., "The boy who lived"
    }
}
