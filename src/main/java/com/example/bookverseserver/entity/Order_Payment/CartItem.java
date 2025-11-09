package com.example.bookverseserver.entity.Order_Payment;

import com.example.bookverseserver.entity.Product.Listing;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "cart_item",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cart_listing", columnNames = {"cart_id", "listing_id"})
        })
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    Listing listing;

    @Column(nullable = false)
    Integer quantity;

    @CreationTimestamp
    @Column(name = "added_at", updatable = false)
    LocalDateTime addedAt;

    public BigDecimal getSubTotalPrice() {
        if (listing == null || listing.getPrice() == null || quantity == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal itemPrice = listing.getPrice();
//        if (listing.getPlatformFeePercent() != null) {
//            BigDecimal fee = itemPrice.multiply(listing.getPlatformFeePercent()).divide(new BigDecimal("100"));
//            itemPrice = itemPrice.add(fee);
//        }

        return itemPrice.multiply(new BigDecimal(quantity));
    }

    public BigDecimal getTotalPrice() {
        return this.cart.getTotalPrice();
    }

    public BigDecimal getDiscountInCart() {
        return this.cart.getVoucher().getDiscountValue()
    }
}
