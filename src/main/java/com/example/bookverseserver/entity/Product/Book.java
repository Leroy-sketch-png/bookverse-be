package com.example.bookverseserver.entity.Product;

import com.example.bookverseserver.entity.User.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "book")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    User seller;

    @Column(nullable = false, length = 255)
    String title;

    String author;

    @Column(unique = true, length = 20)
    String isbn;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;

    String coverImageUrl;

    LocalDate publishedDate;

    @OneToOne(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    Inventory inventory;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Review> reviews;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
