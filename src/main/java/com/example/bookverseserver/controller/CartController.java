package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Cart.CartResponse;
import com.example.bookverseserver.service.CartService;
import com.example.bookverseserver.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@Tag(name = "Cart", description = "Cart management APIs")
public class CartController {

    CartService cartService;
    SecurityUtils securityUtils;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get cart", description = "Get current user's cart with items, total price and applied voucher")
    public ApiResponse<CartResponse> getCart(
            Authentication authentication
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        CartResponse cartResponse = cartService.getCartByUserId(userId);
        return ApiResponse.<CartResponse>builder()
                .code(200)
                .message("Cart retrieved successfully")
                .result(cartResponse)
                .build();
    }

    @PostMapping("/voucher")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Apply voucher", description = "Apply a voucher code to the cart")
    public ApiResponse<CartResponse> applyVoucher(
            Authentication authentication,
            @RequestParam @NotBlank(message = "VOUCHER_CODE_REQUIRED") String voucherCode
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        CartResponse cartResponse = cartService.applyVoucherToCart(userId, voucherCode);
        return ApiResponse.<CartResponse>builder()
                .code(200)
                .message("Voucher applied successfully")
                .result(cartResponse)
                .build();
    }

    @PatchMapping("/voucher")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Remove voucher", description = "Remove the applied voucher from the cart")
    public ApiResponse<CartResponse> removeVoucher(
            Authentication authentication
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        CartResponse cartResponse = cartService.removeVoucherFromCart(userId);
        return ApiResponse.<CartResponse>builder()
                .code(200)
                .message("Voucher removed successfully")
                .result(cartResponse)
                .build();
    }

}
