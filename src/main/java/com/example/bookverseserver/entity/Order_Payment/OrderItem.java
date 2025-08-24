package com.example.bookverseserver.entity.Order_Payment;

import com.example.bookverseserver.entity.Product.Book;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.OrderItemStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;


    @ManyToOne @JoinColumn(name = "order_id")
    Order order;


    @ManyToOne @JoinColumn(name = "listing_id")
    Listing listing;


    @ManyToOne @JoinColumn(name = "book_id")
    Book book;


    @ManyToOne @JoinColumn(name = "seller_id")
    User seller;


    Integer quantity;
    BigDecimal pricePerItem;


    @Enumerated(EnumType.STRING)
    OrderItemStatus itemStatus = OrderItemStatus.PENDING;


    String trackingNumber;
    LocalDateTime shippedAt;
    LocalDateTime deliveredAt;


    @CreationTimestamp
    LocalDateTime createdAt;
}