package com.example.bookverseserver.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Paginated response wrapper matching Vision API_CONTRACTS.md.
 * 
 * Format:
 * {
 *   "data": [...],
 *   "meta": {
 *     "page": 1,
 *     "totalPages": 13,
 *     "totalItems": 245,
 *     "limit": 20,
 *     "hasNext": true,
 *     "hasPrev": false
 *   }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PagedResponse<T> {
    List<T> data;
    PaginationMeta meta;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PaginationMeta {
        Integer page;
        Integer totalPages;
        Long totalItems;
        Integer limit;      // Renamed from itemsPerPage per Vision
        Boolean hasNext;    // Added per Vision
        Boolean hasPrev;    // Added per Vision
    }
    
    /**
     * Helper method to create from Spring's Page.
     * Page numbers are 1-indexed for API consumers (Vision standard).
     */
    public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalElements, int totalPages) {
        PaginationMeta meta = PaginationMeta.builder()
                .page(page + 1)  // Convert 0-indexed to 1-indexed
                .totalPages(totalPages)
                .totalItems(totalElements)
                .limit(size)
                .hasNext(page + 1 < totalPages)
                .hasPrev(page > 0)
                .build();
                
        return PagedResponse.<T>builder()
                .data(content)
                .meta(meta)
                .build();
    }
    
    /**
     * Overload for when you already have 1-indexed page number.
     */
    public static <T> PagedResponse<T> ofOneIndexed(List<T> content, int page, int limit, long totalItems, int totalPages) {
        PaginationMeta meta = PaginationMeta.builder()
                .page(page)
                .totalPages(totalPages)
                .totalItems(totalItems)
                .limit(limit)
                .hasNext(page < totalPages)
                .hasPrev(page > 1)
                .build();
                
        return PagedResponse.<T>builder()
                .data(content)
                .meta(meta)
                .build();
    }
}
