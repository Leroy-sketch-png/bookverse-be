package com.example.bookverseserver.dto.response.Order;

import com.example.bookverseserver.dto.response.ShippingAddress.ShippingAddressResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Checkout Session Response - matches Vision buyer-flow.md
 * 
 * Vision response structure:
 * {
 *   "sessionId": "...",
 *   "cart": { ... },
 *   "shippingAddresses": [ ... ],
 *   "selectedAddressId": 123,
 *   "voucher": { ... },
 *   "expiresAt": "..."
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckoutSessionResponse {
    Long sessionId;
    String status; // PENDING, SHIPPING_SELECTED, READY_FOR_PAYMENT, COMPLETED, EXPIRED
    
    // Cart data (matches Vision cart structure)
    CartDTO cart;
    
    // User's saved shipping addresses (per Vision)
    List<ShippingAddressResponse> shippingAddresses;
    
    // Selected shipping address ID
    Long selectedAddressId;
    
    // Applied voucher info
    VoucherInfoDTO voucher;
    
    LocalDateTime expiresAt;
    LocalDateTime createdAt;
    
    /**
     * Cart DTO nested in checkout session (per Vision)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartDTO {
        Long id;
        List<CartItemDTO> cartItems;
        SummaryDTO summary;
        Integer itemCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemDTO {
        Long id;
        ListingDTO listing;
        Integer quantity;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListingDTO {
        Long id;
        BookDTO book;
        BigDecimal price;
        BigDecimal finalPrice;
        Integer quantity; // available stock
        String condition;
        SellerDTO seller;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookDTO {
        String title;
        String coverImage;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SellerDTO {
        Long id;
        String name;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SummaryDTO {
        BigDecimal subtotal;
        BigDecimal discount;
        BigDecimal total;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VoucherInfoDTO {
        String code;
        String discountType; // PERCENTAGE, FIXED_AMOUNT
        BigDecimal discountValue;
        BigDecimal discountAmount;
    }
}
