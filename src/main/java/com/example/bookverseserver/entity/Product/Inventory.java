package com.example.bookverseserver.entity.Product;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Inventory {

    @Id
    @Column(name = "book_id")
    Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "book_id")
    Book book;

    @Column(nullable = false)
    Integer stockQuantity = 0;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
