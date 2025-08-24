package com.example.bookverseserver.entity.Product;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;


    String title;
    String isbn;
    String description;
    String publisher;
    LocalDate publishedDate;
    String language;
    Integer pages;
    BigDecimal listPrice;


    LocalDateTime deletedAt;
    Long deletedBy;


    @CreationTimestamp
    LocalDateTime createdAt;
    @UpdateTimestamp
    LocalDateTime updatedAt;


    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    List<BookImage> images = new ArrayList<>();


    @ManyToMany
    @JoinTable(
            name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    Set<Author> authors = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;
}