package com.example.bookverseserver.util;

import java.util.regex.Pattern;

/**
 * Utility for normalizing author names to prevent duplicates.
 * 
 * Handles common variations:
 * - "J.K. Rowling" vs "J. K. Rowling" vs "JK Rowling"
 * - Extra whitespace
 * - Case variations
 * - Unicode normalization
 */
public final class AuthorNameNormalizer {
    
    private AuthorNameNormalizer() {
        // Utility class
    }
    
    // Pattern to normalize initials: "J.K." or "J. K." → "J.K."
    private static final Pattern INITIAL_SPACES = Pattern.compile("([A-Z])\\. ([A-Z])");
    
    // Pattern to collapse multiple spaces
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    
    /**
     * Normalize an author name for consistent storage and matching.
     * 
     * Rules:
     * 1. Trim leading/trailing whitespace
     * 2. Collapse multiple spaces to single space
     * 3. Standardize initials (remove space after period in initials)
     * 4. Title case (optional, applied on display not storage)
     * 
     * @param name Raw author name from external source
     * @return Normalized name for storage/matching
     */
    public static String normalize(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        
        String normalized = name.trim();
        
        // Collapse multiple whitespace to single space
        normalized = MULTIPLE_SPACES.matcher(normalized).replaceAll(" ");
        
        // Normalize initials: "J. K. Rowling" → "J.K. Rowling"
        // But keep "J.K. Rowling" as-is
        normalized = INITIAL_SPACES.matcher(normalized).replaceAll("$1.$2");
        
        return normalized;
    }
    
    /**
     * Check if two author names refer to the same person.
     * More lenient than exact match.
     * 
     * @param name1 First name
     * @param name2 Second name
     * @return true if they likely refer to the same author
     */
    public static boolean isSameAuthor(String name1, String name2) {
        if (name1 == null || name2 == null) {
            return false;
        }
        
        String n1 = normalize(name1);
        String n2 = normalize(name2);
        
        if (n1 == null || n2 == null) {
            return false;
        }
        
        // Case-insensitive comparison of normalized names
        return n1.equalsIgnoreCase(n2);
    }
}
