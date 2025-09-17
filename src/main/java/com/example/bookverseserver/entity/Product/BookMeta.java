package com.example.bookverseserver.entity.Product;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.*;

@Entity
@Table(name = "book_meta")
@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = {"authors", "categories"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String title;

    @Column(unique = true)
    String isbn;

    @Column(columnDefinition = "TEXT")
    String description;

    String publisher;
    LocalDate publishedDate;
    Integer pages;
    BigDecimal listPrice;

    LocalDateTime deletedAt;
    Long deletedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    // One-to-Many relationship for images
    @OneToMany(mappedBy = "bookMeta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<BookImage> images = new ArrayList<>();

    // Many-to-Many relationship for authors
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    @JsonManagedReference
    Set<Author> authors = new HashSet<>();

    // Many-to-Many relationship for categories
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_category",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    Set<Category> categories = new HashSet<>();

    public String getCoverImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).getUrl();
        }
        return null;
    }
}