package com.example.bookverseserver.dto.request.Book;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryRequest {
    Long bookId;
    Integer quantityChange;
}
