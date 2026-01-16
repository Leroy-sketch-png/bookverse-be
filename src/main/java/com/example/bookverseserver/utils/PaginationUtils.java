package com.example.bookverseserver.utils;

/**
 * Pagination utilities for consistent safe pagination across all endpoints.
 * 
 * Security: Prevents DoS attacks via excessively large page sizes like ?size=999999
 */
public final class PaginationUtils {
    
    /**
     * Maximum allowed page size to prevent memory exhaustion attacks.
     */
    public static final int MAX_PAGE_SIZE = 100;
    
    /**
     * Default page size when not specified or invalid.
     */
    public static final int DEFAULT_PAGE_SIZE = 20;
    
    private PaginationUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Safely cap the page size to prevent DoS attacks.
     * 
     * @param size The requested page size
     * @return Capped size (1 <= result <= MAX_PAGE_SIZE)
     */
    public static int safeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
    
    /**
     * Safely cap the limit (alias for safeSize).
     * 
     * @param limit The requested limit
     * @return Capped limit (1 <= result <= MAX_PAGE_SIZE)
     */
    public static int safeLimit(int limit) {
        return safeSize(limit);
    }
    
    /**
     * Safely normalize page number (convert 1-indexed to 0-indexed, floor at 0).
     * 
     * @param page The 1-indexed page number from API
     * @return 0-indexed page number for Spring Data
     */
    public static int safePage(int page) {
        return Math.max(0, page - 1);
    }
}
