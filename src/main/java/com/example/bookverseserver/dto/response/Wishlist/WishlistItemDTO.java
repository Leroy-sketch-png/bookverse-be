package com.example.bookverseserver.dto.response.Wishlist;

import com.example.bookverseserver.dto.response.Book.BookResponse;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WishlistItemDTO {
    private Long id;
    private String bookId;
    private BookResponse book;
    private LocalDateTime addedAt;
    private BigDecimal priceAtAddition;
    private BigDecimal currentPrice;
    private BigDecimal priceDrop;
    private double priceDropPercentage;
    private boolean inStock;
}