package com.example.bookverseserver.entity.Product;

import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.BookCondition;
import com.example.bookverseserver.enums.ListingStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "listing")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    BookMeta bookMeta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    User seller;

    @Column(name = "title_override")
    String titleOverride;

    @Column(nullable = false)
    BigDecimal price;

    @Column(nullable = false)
    String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    BookCondition condition;

    @Column(nullable = false)
    Integer quantity = 1;
    String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ListingStatus status = ListingStatus.DRAFT;

    Boolean visibility = true;

    @Column(name = "platform_fee_percent")
    BigDecimal platformFeePercent;

    @Column(name = "suggested_price_low")
    BigDecimal suggestedPriceLow;

    @Column(name = "suggested_price_high")
    BigDecimal suggestedPriceHigh;

    Integer views = 0;
    Integer likes = 0;

    @Column(name = "sold_count")
    Integer soldCount = 0;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    Long deletedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ListingPhoto> photos = new ArrayList<>();
}
