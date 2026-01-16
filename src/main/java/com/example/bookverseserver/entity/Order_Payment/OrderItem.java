package com.example.bookverseserver.entity.Order_Payment;

import com.example.bookverseserver.entity.Product.BookMeta;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.User.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    BookMeta bookMeta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    User seller;

    @Column(nullable = false)
    String title;

    @Column
    String author;

    @Column(name = "cover_image", columnDefinition = "TEXT")
    String coverImage;

    @Column(nullable = false)
    Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal price;

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal subtotal;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    // Helper to convert Items from Cart to Order
    public static OrderItem fromCartItem(CartItem cartItem, Order order) {
        Listing listing = cartItem.getListing();
        BookMeta meta = listing.getBookMeta();

        String authorNames = meta.getAuthors().stream()
                .map(author -> author.getName())
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);

        String cover = null;
        if (!listing.getPhotos().isEmpty()) {
            cover = listing.getPhotos().get(0).getUrl();
        } else if (meta.getImages() != null && !meta.getImages().isEmpty()) {
            cover = meta.getImages().stream().findFirst().map(img -> img.getUrl()).orElse(null);
        }

        return OrderItem.builder()
                .order(order)
                .listing(listing)
                .bookMeta(meta)
                .seller(listing.getSeller())
                .title(listing.getTitleOverride() != null ? listing.getTitleOverride() : meta.getTitle())
                .author(authorNames)
                .coverImage(cover)
                .quantity(cartItem.getQuantity())
                .price(listing.getPrice())
                .subtotal(listing.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .build();
    }
}
