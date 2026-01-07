package com.example.bookverseserver.dto.response.Product;

import com.example.bookverseserver.dto.response.Book.AuthorResponse;
import com.example.bookverseserver.dto.response.Book.CategoryResponse;
import com.example.bookverseserver.entity.Product.ListingPhoto;
import com.example.bookverseserver.enums.BookCondition;
import com.example.bookverseserver.enums.ListingStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListingResponse {

    Long id;

    // Book information
    Long bookMetaId;
    String bookTitle;
    List<AuthorResponse> authors; // Author details
    String bookCoverImage;
    String isbn;

    // Category information
    CategoryResponse category;

    Long sellerId;
    String sellerName;

    String titleOverride;
    BigDecimal price;           // Base/list price
    BigDecimal originalPrice;   // Original price before discount (if any)
    BigDecimal finalPrice;      // Actual selling price (per Vision API_CONTRACTS.md)
    String currency;

    BookCondition condition;
    Integer quantity;
    String location;

    ListingStatus status;
    Boolean visibility;

    BigDecimal platformFeePercent;
    BigDecimal suggestedPriceLow;
    BigDecimal suggestedPriceHigh;

    Integer views;
    Integer likes;
    Integer soldCount;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    List<ListingPhotoResponse> photos;
    }
