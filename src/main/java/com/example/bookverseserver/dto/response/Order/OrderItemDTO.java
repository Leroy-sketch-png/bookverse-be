package com.example.bookverseserver.dto.response.Order;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemDTO {
  Long id;
  Long bookId;
  String title;
  String author;
  String coverImage;
  Integer quantity;
  BigDecimal price;
  BigDecimal subtotal;
  public SellerInfo seller;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SellerInfo {
    Long id;
    String name;
    String slug;
  }
}
