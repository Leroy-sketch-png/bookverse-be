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
    String slug; // URL-friendly name
    String description;
    List<String> tags;
    LocalDateTime createdAt;
    
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
