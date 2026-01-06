package com.example.bookverseserver.dto.response.Order;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderTrackingDTO {
  Long orderId;
  String orderNumber;
  String trackingNumber;
  String carrier;
  String trackingUrl;
  String status;
  LocalDateTime estimatedDelivery;
  List<TrackingEvent> events;

  @Data
  @Builder
  public static class TrackingEvent {
    String status;
    String location;
    LocalDateTime timestamp;
    String description;
  }
}
