package com.example.bookverseserver.dto.response.User;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerRegistrationResponse {
    Long id;
    String shopName;
    String status; // ACTIVE, PENDING, etc.
    LocalDateTime registeredAt;
}
