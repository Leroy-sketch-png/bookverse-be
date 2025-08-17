package com.example.bookverseserver.entity.Order_Payment;

import com.example.bookverseserver.entity.Product.Book;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_item", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"cart_id", "book_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    Cart cart;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    Book book;

    @Column(nullable = false)
    Integer quantity;

    LocalDateTime addedAt;
}
