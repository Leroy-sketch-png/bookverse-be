package com.example.bookverseserver.enums;

/**
 * Enum representing stock update operations for listings.
 */
public enum StockOperation {
    SET, // Set stock to exact quantity
    ADD, // Add to current stock
    SUBTRACT // Subtract from current stock
}
