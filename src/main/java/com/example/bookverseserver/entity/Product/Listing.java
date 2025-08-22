package com.example.bookverseserver.entity.Product;

import com.example.bookverseserver.entity.User.User;
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
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Listing {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;


    @ManyToOne @JoinColumn(name = "book_id")
    Book book;


    @ManyToOne @JoinColumn(name = "seller_id")
    User seller;


    String titleOverride;
    BigDecimal price;
    String currency;


    @Enumerated(EnumType.STRING)
    BookCondition condition;


    Integer quantity = 1;
    String location;


    @Enumerated(EnumType.STRING)
    ListingStatus status = ListingStatus.DRAFT;


    Boolean visibility = true;
    BigDecimal platformFeePercent;
    BigDecimal suggestedPriceLow;
    BigDecimal suggestedPriceHigh;
    Integer views = 0;
    Integer likes = 0;
    Integer soldCount = 0;


    LocalDateTime deletedAt;
    Long deletedBy;


    @CreationTimestamp
    LocalDateTime createdAt;
    @UpdateTimestamp
    LocalDateTime updatedAt;


    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ListingPhoto> photos = new ArrayList<>();
}