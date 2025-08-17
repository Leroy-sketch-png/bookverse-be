package com.example.bookverseserver.dto.response.Book;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryResponse {
    Long bookId;
    String bookTitle;
    Integer stockQuantity;
    LocalDateTime updatedAt;
}
