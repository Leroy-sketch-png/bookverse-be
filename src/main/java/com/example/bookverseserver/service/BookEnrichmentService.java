package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.External.RichBookData;
import com.example.bookverseserver.util.ExternalCategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * THE CUNNING BOOK ENRICHMENT ORCHESTRATOR.
 * 
 * This is the brain of our multi-source strategy:
 * 1. Try Open Library first (best for public domain, academic, older books)
 * 2. Fall back to Google Books (best for commercial, modern books)
 * 3. Merge data from both sources to fill gaps
 * 4. Normalize chaotic categories to our canonical 10 using CategoryMapper
 * 5. Calculate data quality score
 * 
 * The goal: NEVER return incomplete data if any source has it.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookEnrichmentService {

    private final OpenLibraryService openLibraryService;
    private final GoogleBooksService googleBooksService;

    /**
     * Fetch and enrich book data from multiple sources.
     * 
     * @param isbn ISBN-10 or ISBN-13
     * @return EnrichedBookResult with merged data and quality score
     */
    public EnrichedBookResult fetchEnrichedBookData(String isbn) {
        String cleanIsbn = cleanIsbn(isbn);
        
        log.info("Enriching book data for ISBN: {}", cleanIsbn);
        
        // Try both sources in parallel conceptually (but sequential for simplicity)
        RichBookData openLibraryData = openLibraryService.fetchRichBookDetailsByIsbn(cleanIsbn);
        RichBookData googleData = googleBooksService.fetchBookByIsbn(cleanIsbn);
        
        if (openLibraryData == null && googleData == null) {
            log.warn("No book data found for ISBN {} from any source", cleanIsbn);
            return EnrichedBookResult.notFound(cleanIsbn);
        }
        
        // Merge data: Open Library is primary, Google fills gaps
        RichBookData merged = mergeBookData(openLibraryData, googleData, cleanIsbn);
        
        // Apply category normalization - THE CUNNING PART
        List<String> canonicalCategories = normalizeCategories(merged.getCategories());
        merged = merged.toBuilder()
                .categories(canonicalCategories)
                .build();
        
        // Calculate quality score
        int qualityScore = calculateQualityScore(merged);
        String primarySource = openLibraryData != null ? "OPEN_LIBRARY" : "GOOGLE_BOOKS";
        boolean hasSecondaryData = openLibraryData != null && googleData != null;
        
        log.info("Book enrichment complete for ISBN {}. Quality: {}/100, Primary: {}, Merged: {}", 
                cleanIsbn, qualityScore, primarySource, hasSecondaryData);
        
        return EnrichedBookResult.builder()
                .found(true)
                .data(merged)
                .qualityScore(qualityScore)
                .primarySource(primarySource)
                .mergedFromMultipleSources(hasSecondaryData)
                .build();
    }

    /**
     * Merge data from two sources, preferring Open Library but filling gaps from Google.
     */
    private RichBookData mergeBookData(RichBookData primary, RichBookData secondary, String isbn) {
        if (primary == null && secondary == null) {
            return null;
        }
        if (primary == null) {
            return secondary;
        }
        if (secondary == null) {
            return primary;
        }
        
        // Merge: take primary unless empty, then take secondary
        return RichBookData.builder()
                .isbn(isbn)
                .title(coalesce(primary.getTitle(), secondary.getTitle()))
                .description(coalesceDescription(primary.getDescription(), secondary.getDescription()))
                .publisher(coalesce(primary.getPublisher(), secondary.getPublisher()))
                .publishedDate(coalesce(primary.getPublishedDate(), secondary.getPublishedDate()))
                .numberOfPages(coalesceInt(primary.getNumberOfPages(), secondary.getNumberOfPages()))
                .coverUrl(coalesceCover(primary.getCoverUrl(), secondary.getCoverUrl()))
                .authors(mergeAuthors(primary.getAuthors(), secondary.getAuthors()))
                .categories(mergeCategories(primary.getCategories(), secondary.getCategories()))
                // Rich fields only from Open Library
                .firstLine(primary.getFirstLine())
                .subjectPlaces(primary.getSubjectPlaces())
                .subjectPeople(primary.getSubjectPeople())
                .subjectTimes(primary.getSubjectTimes())
                .externalLinks(primary.getExternalLinks())
                .openLibraryId(primary.getOpenLibraryId())
                .goodreadsId(primary.getGoodreadsId())
                .build();
    }

    private String coalesce(String a, String b) {
        return isNotEmpty(a) ? a : b;
    }

    private String coalesceDescription(String a, String b) {
        // Prefer longer description (more content = better)
        if (isNotEmpty(a) && isNotEmpty(b)) {
            return a.length() >= b.length() ? a : b;
        }
        return coalesce(a, b);
    }

    private int coalesceInt(int a, int b) {
        return a > 0 ? a : b;
    }

    private String coalesceCover(String a, String b) {
        // Prefer Google's cover (usually higher resolution)
        // But if we have Open Library and not Google, use Open Library
        return isNotEmpty(b) ? b : a; // Note: b is secondary (Google) - preferred for covers
    }

    private List<String> mergeAuthors(List<String> a, List<String> b) {
        if (a != null && !a.isEmpty()) {
            return a;
        }
        return b != null ? b : Collections.emptyList();
    }

    private List<String> mergeCategories(List<String> a, List<String> b) {
        // Combine all categories for better mapping
        List<String> merged = new ArrayList<>();
        if (a != null) {
            merged.addAll(a);
        }
        if (b != null) {
            merged.addAll(b);
        }
        return merged;
    }

    private boolean isNotEmpty(String s) {
        return s != null && !s.isBlank();
    }

    /**
     * Normalize chaotic external categories to our canonical 10.
     */
    private List<String> normalizeCategories(List<String> rawCategories) {
        if (rawCategories == null || rawCategories.isEmpty()) {
            return Collections.emptyList();
        }
        return ExternalCategoryMapper.mapToCanonical(rawCategories);
    }

    /**
     * Calculate a quality score (0-100) based on data completeness.
     */
    private int calculateQualityScore(RichBookData data) {
        if (data == null) {
            return 0;
        }
        
        int score = 0;
        
        // Essential fields (60 points total)
        if (isNotEmpty(data.getTitle())) score += 15;
        if (isNotEmpty(data.getIsbn())) score += 10;
        if (data.getAuthors() != null && !data.getAuthors().isEmpty()) score += 15;
        if (isNotEmpty(data.getDescription())) score += 10;
        if (isNotEmpty(data.getCoverUrl())) score += 10;
        
        // Important fields (25 points total)
        if (isNotEmpty(data.getPublisher())) score += 5;
        if (isNotEmpty(data.getPublishedDate())) score += 5;
        if (data.getNumberOfPages() > 0) score += 5;
        if (data.getCategories() != null && !data.getCategories().isEmpty()) score += 10;
        
        // Rich metadata fields (15 points total)
        if (isNotEmpty(data.getFirstLine())) score += 5;
        if (isNotEmpty(data.getOpenLibraryId())) score += 3;
        if (isNotEmpty(data.getGoodreadsId())) score += 2;
        if (data.getSubjectPlaces() != null && !data.getSubjectPlaces().isEmpty()) score += 2;
        if (data.getSubjectPeople() != null && !data.getSubjectPeople().isEmpty()) score += 2;
        if (data.getSubjectTimes() != null && !data.getSubjectTimes().isEmpty()) score += 1;
        
        return Math.min(100, score);
    }

    private String cleanIsbn(String isbn) {
        if (isbn == null) {
            return "";
        }
        // Remove hyphens and spaces
        return isbn.replaceAll("[\\s-]", "");
    }

    /**
     * Result wrapper with quality metadata.
     */
    @lombok.Builder
    @lombok.Data
    public static class EnrichedBookResult {
        private boolean found;
        private RichBookData data;
        private int qualityScore;
        private String primarySource;
        private boolean mergedFromMultipleSources;
        
        public static EnrichedBookResult notFound(String isbn) {
            return EnrichedBookResult.builder()
                    .found(false)
                    .data(null)
                    .qualityScore(0)
                    .primarySource("NONE")
                    .mergedFromMultipleSources(false)
                    .build();
        }
    }
}
