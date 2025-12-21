package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.CartItem.CartItemRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.CartItem.CartItemResponse;
import com.example.bookverseserver.service.CartItemService;
import com.example.bookverseserver.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/v1/cart/items")
@Tag(name = "Cart Item", description = "Cart item management APIs")
public class CartItemController {

    CartItemService cartItemService;
    SecurityUtils securityUtils;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Add item to cart", description = "Add a listing to the cart or increase quantity if already exists")
    public ApiResponse<CartItemResponse> createCartItem(
            @RequestBody CartItemRequest cartItemRequest,
            Authentication authentication
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        CartItemResponse cartItemResponse = cartItemService.createCartItem(userId, cartItemRequest);
        return ApiResponse.<CartItemResponse>builder()
                .code(200)
                .message("Cart item created successfully")
                .result(cartItemResponse)
                .build();
    }

    @PutMapping("/{listingId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update cart item quantity", description = "Update the quantity of a cart item. Set quantity to 0 to remove the item")
    public ApiResponse<CartItemResponse> updateCartItem(
            @PathVariable("listingId") Long listingId,
            Authentication authentication,
            @RequestParam Integer quantity
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        CartItemResponse cartItemResponse = cartItemService.updateCartItem(userId, listingId, quantity);
        return ApiResponse.<CartItemResponse>builder()
                .code(200)
                .message("Cart item updated successfully")
                .result(cartItemResponse)
                .build();
    }

    @DeleteMapping("/{listingId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Remove item from cart", description = "Remove a listing from the cart")
    public ApiResponse<CartItemResponse> deleteCartItem(
            @PathVariable("listingId") Long listingId,
            Authentication authentication
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        CartItemResponse cartItemResponse = cartItemService.deleteCartItem(userId, listingId);
        return ApiResponse.<CartItemResponse>builder()
                .code(200)
                .message("Cart item deleted successfully")
                .result(cartItemResponse)
                .build();
    }

}
