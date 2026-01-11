package com.example.bookverseserver.dto.response.Order;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderListResponse {
  List<OrderDTO> data;
  PaginationMeta meta;

  @Data
  @Builder
  public static class PaginationMeta {
    int page;
    int limit;
    long totalItems;
    int totalPages;
    boolean hasNext;
    boolean hasPrev;
  }
}
