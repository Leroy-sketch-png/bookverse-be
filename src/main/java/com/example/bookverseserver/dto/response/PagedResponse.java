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
        Integer itemsPerPage;
    }
    
    // Helper method to create from Spring's Page
    public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalElements, int totalPages) {
        PaginationMeta meta = PaginationMeta.builder()
                .page(page)
                .totalPages(totalPages)
                .totalItems(totalElements)
                .itemsPerPage(size)
                .build();
                
        return PagedResponse.<T>builder()
                .data(content)
                .meta(meta)
                .build();
    }
}
