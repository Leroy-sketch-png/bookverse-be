package com.example.bookverseserver.dto.response.Wishlist;

import com.example.bookverseserver.dto.response.Product.ListingSummaryResponse;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WishlistItemDTO {
    private Long id;

    private ListingSummaryResponse listing;

    private LocalDateTime addedAt;
    private BigDecimal priceAtAddition;
    private BigDecimal currentPrice;
    private BigDecimal priceDrop;
    private double priceDropPercentage;

    private boolean inStock;
}