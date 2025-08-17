package com.example.bookverseserver.entity.Order_Payment;

import com.example.bookverseserver.entity.Product.Book;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    Book book;

    @Column(nullable = false)
    Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal price;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
