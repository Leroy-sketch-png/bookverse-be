package com.example.bookverseserver.entity.Product;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Granular genre/subject tags extracted from Open Library.
 * 
 * Unlike Categories (6 broad buckets: Fiction, Non-Fiction, Science, etc.),
 * Tags provide fine-grained discoverability:
 * - "romance", "historical", "regency", "mystery", "literary fiction", "coming of age"
 * 
 * This enables powerful filtering: "Show me Romance + Historical fiction"
 */
@Entity
@Table(name = "book_tag", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"slug"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 100)
    String name;  // Display name: "Literary Fiction"

    @Column(nullable = false, unique = true, length = 100)
    String slug;  // URL-safe: "literary-fiction"
    
    @Column(name = "usage_count")
    @Builder.Default
    Integer usageCount = 0;  // How many books use this tag (for sorting)
}
