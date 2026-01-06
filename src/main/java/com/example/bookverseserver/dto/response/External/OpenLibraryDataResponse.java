
package com.example.bookverseserver.dto.response.External;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Complete Open Library API response mapping.
 * 
 * This captures the FULL VALUE from Open Library's jscmd=data format:
 * - Basic: title, authors, pages, publisher, publish_date
 * - Rich: subjects, subject_places, subject_people, subject_times
 * - Marketing: excerpts (first lines!), links (Wikipedia, etc.)
 * - Cross-platform: identifiers (goodreads, oclc, etc.)
 * - Media: cover URLs in 3 sizes
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryDataResponse {

    private String key; // e.g., /books/OL1017798M
    private String url; // Full URL to Open Library page
    private String title;
    private List<Author> authors;
    
    @JsonProperty("number_of_pages")
    private int numberOfPages;
    
    private List<Publisher> publishers;
    
    @JsonProperty("publish_date")
    private String publishDate;
    
    @JsonProperty("publish_places")
    private List<PublishPlace> publishPlaces;
    
    // ═══════════════════════════════════════════════════════════════════════════
    // RICH SUBJECT DATA - The hidden gold
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Main subjects - contains genres, themes, descriptors
     * e.g., "Romance fiction", "Historical fiction", "Literary Fiction", "Fiction Classics"
     */
    private List<Subject> subjects;
    
    /**
     * Story locations - where the story takes place
     * e.g., "England", "Derbyshire", "Hertfordshire", "Pemberley"
     */
    @JsonProperty("subject_places")
    private List<SubjectPlace> subjectPlaces;
    
    /**
     * Characters - who appears in the story
     * e.g., "Elizabeth Bennet", "Fitzwilliam Darcy", "Jane Bennet"
     */
    @JsonProperty("subject_people")
    private List<SubjectPerson> subjectPeople;
    
    /**
     * Time periods - when the story is set
     * e.g., "19th century", "1789-1820"
     */
    @JsonProperty("subject_times")
    private List<SubjectTime> subjectTimes;
    
    // ═══════════════════════════════════════════════════════════════════════════
    // MARKETING & CROSS-PLATFORM DATA
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Famous opening lines and notable passages
     * e.g., "It is a truth universally acknowledged..."
     */
    private List<Excerpt> excerpts;
    
    /**
     * External links - Wikipedia, Britannica, author sites
     */
    private List<ExternalLink> links;
    
    /**
     * Cross-platform identifiers - goodreads, oclc, isbn_10, isbn_13, etc.
     */
    private Identifiers identifiers;
    
    /**
     * Cover images in 3 sizes
     */
    private Cover cover;

    // ═══════════════════════════════════════════════════════════════════════════
    // NESTED CLASSES
    // ═══════════════════════════════════════════════════════════════════════════

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        private String url; // e.g., https://openlibrary.org/authors/OL21594A/Jane_Austen
        private String name;
        
        /**
         * Extracts the OpenLibrary author key from the URL.
         * Example: "https://openlibrary.org/authors/OL21594A/Jane_Austen" → "/authors/OL21594A"
         */
        public String getKey() {
            if (url == null || url.isEmpty()) return null;
            try {
                java.net.URL parsed = new java.net.URL(url);
                String path = parsed.getPath();
                String[] parts = path.split("/");
                if (parts.length >= 3) {
                    return "/" + parts[1] + "/" + parts[2];
                }
                return path;
            } catch (Exception e) {
                return null;
            }
        }
        
        /**
         * Extracts just the author ID: "OL21594A"
         */
        public String getAuthorId() {
            String key = getKey();
            if (key == null) return null;
            String[] parts = key.split("/");
            return parts.length >= 3 ? parts[2] : null;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Publisher {
        private String name;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublishPlace {
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Subject {
        private String name;
        private String url;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubjectPlace {
        private String name;
        private String url;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubjectPerson {
        private String name;
        private String url;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubjectTime {
        private String name;
        private String url;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Excerpt {
        private String text;
        private String comment; // e.g., "first sentence"
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExternalLink {
        private String title;
        private String url;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Identifiers {
        @JsonProperty("isbn_10")
        private List<String> isbn10;
        
        @JsonProperty("isbn_13")
        private List<String> isbn13;
        
        private List<String> goodreads;
        private List<String> oclc;
        private List<String> openlibrary;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cover {
        private String small;
        private String medium;
        private String large;
    }
}
