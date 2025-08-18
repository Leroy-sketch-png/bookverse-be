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

    // seller_id (NOT NULL, FK to User)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    User seller;

    // author_id (FK to Author, nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    Author author;

    // category_id (FK to Category, nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    Category category;

    @Column(nullable = false, length = 255)
    String title;

    @Column(length = 20, nullable = false, unique = true)
    String isbn;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal price;   // selling price

    @Column(name = "list_price", precision = 10, scale = 2)
    BigDecimal listPrice; // strikethrough/original price

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    Condition condition;

    @Column(name = "cover_image_url", columnDefinition = "TEXT")
    String coverImageUrl;

    LocalDate publishedDate;

    @Column(name = "stock_quantity", nullable = false)
    Integer stockQuantity = 1;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Review> reviews;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    // Enum for condition values (must match SQL CHECK constraint)
    public enum Condition {
        NEW("New"),
        LIKE_NEW("Like New"),
        USED("Used"),
        ACCEPTABLE("Acceptable");

        private final String dbValue;

        Condition(String dbValue) {
            this.dbValue = dbValue;
        }

        @Override
        public String toString() {
            return dbValue;
        }
    }
}
