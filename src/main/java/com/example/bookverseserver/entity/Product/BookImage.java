package com.example.bookverseserver.entity.Product;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_image") // Mapped to book_image table
@Getter
@Setter
@ToString(exclude = "bookMeta")
@EqualsAndHashCode(exclude = "bookMeta")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Use LAZY fetch to optimize performance
    @JoinColumn(name = "book_id", nullable = false) // Book_id is NOT NULL in SQL
    BookMeta bookMeta;

    @Column(nullable = false) // URL is NOT NULL in SQL
    String url;
    String altText;
    Boolean isCover = false;
    Integer position = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false) // Align with column name and updatable setting
    LocalDateTime createdAt;
}