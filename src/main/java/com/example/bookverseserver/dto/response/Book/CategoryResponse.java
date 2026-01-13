package com.example.bookverseserver.dto.response.Book;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse {
    Long id;
    String name;
    String slug;
    String description;
    List<String> tags;
    LocalDateTime createdAt;
    
    // ═══════════════════════════════════════════════════════════════════════
    // HIERARCHY & DISPLAY (Renaissance Edition)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Parent category ID (null for top-level)
     */
    Long parentId;
    
    /**
     * Emoji icon for visual richness
     */
    String emoji;
    
    /**
     * Display order within parent
     */
    Integer sortOrder;
    
    /**
     * Theme color (hex)
     */
    String color;
    
    /**
     * Whether featured on homepage
     */
    Boolean featured;
    
    /**
     * Approximate book count
     */
    Integer bookCount;
    
    /**
     * Nested child categories (for tree response)
     */
    List<CategoryResponse> children;
    
    // Legacy constructors for backward compatibility
    public CategoryResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public CategoryResponse(Long id, String name, String slug) {
        this.id = id;
        this.name = name;
        this.slug = slug;
    }
}
