package com.example.bookverseserver.entity.Order_Payment;

import com.example.bookverseserver.entity.Product.BookMeta;
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

    // Many order items belong to one order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    // The listing that was purchased
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    Listing listing;

    // The metadata for the book purchased
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    BookMeta bookMeta;

    // The user who sold this item
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    User seller;

    // The quantity of the item
    @Column(nullable = false)
    Integer quantity;

    // The price of a single item at the time of purchase
    @Column(name = "price_per_item", nullable = false)
    BigDecimal pricePerItem;

    // The status of the item within the order lifecycle
    @Enumerated(EnumType.STRING)
    @Column(name = "item_status", nullable = false)
    OrderItemStatus itemStatus = OrderItemStatus.PENDING;

    // Tracking information
    @Column(name = "tracking_number")
    String trackingNumber;

    @Column(name = "shipped_at")
    LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    LocalDateTime deliveredAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    // Helper to convert Items from Cart to Order
    public static OrderItem fromCartItem(CartItem cartItem, Order order) {
        Listing listing = cartItem.getListing();

        return OrderItem.builder()
                .order(order)
                .listing(listing)
                .bookMeta(listing.getBookMeta())
                .seller(listing.getSeller())
                .quantity(cartItem.getQuantity())
                .pricePerItem(listing.getPrice())
                .itemStatus(OrderItemStatus.PENDING)
                .build();
    }
}
