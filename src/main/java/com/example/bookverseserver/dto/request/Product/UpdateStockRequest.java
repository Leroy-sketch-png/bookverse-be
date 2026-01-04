package com.example.bookverseserver.dto.request.Product;

import com.example.bookverseserver.enums.StockOperation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for updating listing stock.
 * Supports SET (exact value), ADD (increment), and SUBTRACT (decrement)
 * operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateStockRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    Integer quantity;

    @NotNull(message = "Operation is required")
    StockOperation operation;
}
