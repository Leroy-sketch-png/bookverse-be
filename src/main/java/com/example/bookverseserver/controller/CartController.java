package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Cart.CartResponse;
import com.example.bookverseserver.service.CartService;
import com.example.bookverseserver.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/api/cart")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@Tag(name = "Shopping Cart", description = "🛒 Shopping cart management APIs - View cart, apply vouchers, calculate totals")
public class CartController {

    CartService cartService;
    SecurityUtils securityUtils;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Get shopping cart",
        description = "Retrieve current user's cart with complete details: " +
                     "- All cart items with book info and prices " +
                     "- Subtotal, tax, and shipping costs " +
                     "- Applied voucher discount " +
                     "- Final total amount " +
                     "- Stock availability for each item"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Cart retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Cart not found (will create empty cart)"
        )
    })
    public ApiResponse<CartResponse> getCart(
            Authentication authentication
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        CartResponse cartResponse = cartService.getCartByUserId(userId);
        return ApiResponse.<CartResponse>builder()
                .code(1000)
                .message("Cart retrieved successfully")
                .result(cartResponse)
                .build();
    }

    @PostMapping("/voucher")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Apply voucher to cart",
        description = "Apply a discount voucher code to the shopping cart. " +
                     "**Voucher types**: " +
                     "- Percentage discount (e.g., 20% off) " +
                     "- Fixed amount discount (e.g., $10 off) " +
                     "- Free shipping " +
                     "**Validations**: Expiry date, minimum order amount, usage limits"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Voucher applied successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid voucher (expired, min order not met, usage limit reached)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Voucher code not found"
        )
    })
    public ApiResponse<CartResponse> applyVoucher(
            Authentication authentication,
            
            @Parameter(description = "Voucher code (e.g., SUMMER20, FREESHIP)", example = "SUMMER20", required = true)
            @RequestParam @NotBlank(message = "VOUCHER_CODE_REQUIRED") String voucherCode
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        CartResponse cartResponse = cartService.applyVoucherToCart(userId, voucherCode);
        return ApiResponse.<CartResponse>builder()
                .code(1000)
                .message("Voucher applied successfully")
                .result(cartResponse)
                .build();
    }

    @PatchMapping("/voucher")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Remove voucher from cart",
        description = "Remove the currently applied voucher and recalculate cart total. " +
                     "Cart will revert to original pricing without discount."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Voucher removed successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Cart not found or no voucher applied"
        )
    })
    public ApiResponse<CartResponse> removeVoucher(
            Authentication authentication
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        CartResponse cartResponse = cartService.removeVoucherFromCart(userId);
        return ApiResponse.<CartResponse>builder()
                .code(1000)
                .message("Voucher removed successfully")
                .result(cartResponse)
                .build();
    }

}
