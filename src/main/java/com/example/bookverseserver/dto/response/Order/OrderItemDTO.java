package com.example.bookverseserver.dto.response.Order;

import com.example.bookverseserver.enums.BookCondition;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * Order item DTO matching Vision API_CONTRACTS.md
 * 
 * FE expects nested listing: { id, book: { title, coverImage }, condition }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemDTO {
  Long id;
  ListingInfo listing;
  Integer quantity;
  BigDecimal priceAtPurchase;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ListingInfo {
    Long id;
    BookInfo book;
    BookCondition condition;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BookInfo {
    String title;
    String coverImage;
  }
}
