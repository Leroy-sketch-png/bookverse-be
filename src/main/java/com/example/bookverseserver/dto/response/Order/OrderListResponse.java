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
  List<OrderDTO> orders;
  PaginationInfo pagination;

  @Data
  @Builder
  public static class PaginationInfo {
    int page;
    int limit;
    long totalItems;
    int totalPages;
    boolean hasNext;
    boolean hasPrevious;
  }
}
