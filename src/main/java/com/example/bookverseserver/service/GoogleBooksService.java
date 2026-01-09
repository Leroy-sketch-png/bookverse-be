package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.External.RichBookData;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Google Books API integration - THE CUNNING FALLBACK.
 * 
 * When Open Library fails or returns incomplete data, we fall back to Google Books.
 * Google Books has better coverage for modern books and commercial titles.
 * 
 * API: https://www.googleapis.com/books/v1/volumes?q=isbn:XXXXXXXXXX
 * Free tier: 1000 requests/day (no API key needed for public data)
 * 
 * Strategy:
 * - Use as SECONDARY source after Open Library
 * - Merge data: prefer Open Library's rich metadata, use Google for gaps
 * - Google is better for: covers, descriptions, commercial books
 * - Open Library is better for: public domain, academic, older books
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleBooksService {

    private static final String GOOGLE_BOOKS_API = "https://www.googleapis.com/books/v1/volumes";
    private final RestTemplate restTemplate;

    /**
     * Fetch book data from Google Books API by ISBN.
     * 
     * @param isbn ISBN-10 or ISBN-13 (without hyphens)
     * @return RichBookData or null if not found
     */
    public RichBookData fetchBookByIsbn(String isbn) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(GOOGLE_BOOKS_API)
                    .queryParam("q", "isbn:" + isbn)
                    .toUriString();

            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            
            if (response == null || !response.has("items") || response.get("items").isEmpty()) {
                log.debug("Google Books: No results for ISBN {}", isbn);
                return null;
            }

            // Take first result (most relevant)
            JsonNode item = response.get("items").get(0);
            JsonNode volumeInfo = item.get("volumeInfo");

            if (volumeInfo == null) {
                return null;
            }

            return parseVolumeInfo(isbn, volumeInfo);

        } catch (Exception e) {
            log.warn("Google Books API error for ISBN {}: {}", isbn, e.getMessage());
            return null;
        }
    }

    private RichBookData parseVolumeInfo(String isbn, JsonNode volumeInfo) {
        // Title
        String title = getTextOrNull(volumeInfo, "title");
        if (title == null) {
            return null; // At minimum we need a title
        }

        // Authors
        List<String> authors = new ArrayList<>();
        if (volumeInfo.has("authors") && volumeInfo.get("authors").isArray()) {
            volumeInfo.get("authors").forEach(a -> authors.add(a.asText()));
        }

        // Categories (Google's categories are cleaner than Open Library's)
        List<String> categories = new ArrayList<>();
        if (volumeInfo.has("categories") && volumeInfo.get("categories").isArray()) {
            volumeInfo.get("categories").forEach(c -> categories.add(c.asText()));
        }

        // Description
        String description = getTextOrNull(volumeInfo, "description");

        // Publisher
        String publisher = getTextOrNull(volumeInfo, "publisher");

        // Published Date (Google uses YYYY-MM-DD or YYYY)
        String publishedDate = getTextOrNull(volumeInfo, "publishedDate");

        // Page count
        int pageCount = volumeInfo.has("pageCount") ? volumeInfo.get("pageCount").asInt(0) : 0;

        // Cover image (prefer extraLarge > large > medium > small > thumbnail)
        String coverUrl = extractBestCover(volumeInfo);

        // Language
        String language = getTextOrNull(volumeInfo, "language");

        return RichBookData.builder()
                .title(title)
                .isbn(isbn)
                .description(description)
                .publisher(publisher)
                .publishedDate(publishedDate)
                .numberOfPages(pageCount)
                .coverUrl(coverUrl)
                .authors(authors)
                .categories(categories)
                // Google doesn't provide these rich fields
                .firstLine(null)
                .subjectPlaces(Collections.emptyList())
                .subjectPeople(Collections.emptyList())
                .subjectTimes(Collections.emptyList())
                .externalLinks(Collections.emptyList())
                .openLibraryId(null) // Not from Open Library
                .goodreadsId(null)   // Google doesn't provide this
                .build();
    }

    private String extractBestCover(JsonNode volumeInfo) {
        if (!volumeInfo.has("imageLinks")) {
            return null;
        }

        JsonNode imageLinks = volumeInfo.get("imageLinks");
        
        // Priority: extraLarge > large > medium > small > thumbnail
        String[] priorities = {"extraLarge", "large", "medium", "small", "thumbnail"};
        
        for (String size : priorities) {
            if (imageLinks.has(size)) {
                String url = imageLinks.get(size).asText();
                // Google returns HTTP, we want HTTPS
                return url.replace("http://", "https://");
            }
        }
        
        return null;
    }

    private String getTextOrNull(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            String text = node.get(field).asText();
            return text.isEmpty() ? null : text;
        }
        return null;
    }
}
