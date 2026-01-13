package com.example.bookverseserver.entity.Product;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false, length = 100)
    String name;
    
    @Column(unique = true, nullable = false, length = 150)
    String slug; // URL-friendly version of name

    @Column(columnDefinition = "TEXT")
    String description;
    
    // ═══════════════════════════════════════════════════════════════════════
    // HIERARCHY & DISPLAY (Renaissance Edition)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Parent category for hierarchical structure.
     * null = top-level category
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    Category parent;
    
    /**
     * Child categories
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    List<Category> children = new ArrayList<>();
    
    /**
     * Emoji icon for visual richness (e.g., "📚", "🔍", "💕")
     */
    @Column(length = 10)
    String emoji;
    
    /**
     * Display order within parent (lower = first)
     */
    @Column(name = "sort_order")
    @Builder.Default
    Integer sortOrder = 0;
    
    /**
     * Theme color for category cards (hex, e.g., "#6366F1")
     */
    @Column(length = 7)
    String color;
    
    /**
     * Whether this category is featured on homepage
     */
    @Builder.Default
    Boolean featured = false;
    
    /**
     * Approximate book count (denormalized for performance)
     */
    @Column(name = "book_count")
    @Builder.Default
    Integer bookCount = 0;

    // ═══════════════════════════════════════════════════════════════════════
    // RELATIONSHIPS
    // ═══════════════════════════════════════════════════════════════════════

    @ManyToMany(mappedBy = "categories")
    Set<BookMeta> bookMetas;

    List<String> tags;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
