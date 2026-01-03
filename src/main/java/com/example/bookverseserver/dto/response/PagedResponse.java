package com.example.bookverseserver.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PagedResponse<T> {
    List<T> data;
    PaginationMeta pagination;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PaginationMeta {
        Integer total;
        Integer page;
        Integer limit;
        Boolean hasNext;
        Integer totalPages;
    }
    
    // Helper method to create from Spring's Page
    public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalElements, int totalPages) {
        PaginationMeta meta = PaginationMeta.builder()
                .total((int) totalElements)
                .page(page)
                .limit(size)
                .hasNext(page < totalPages - 1)
                .totalPages(totalPages)
                .build();
                
        return PagedResponse.<T>builder()
                .data(content)
                .pagination(meta)
                .build();
    }
}
