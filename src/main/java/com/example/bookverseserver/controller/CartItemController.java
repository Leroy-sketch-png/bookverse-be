package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.CartItem.CartItemRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.CartItem.CartItemResponse;
import com.example.bookverseserver.service.CartItemService;
import com.example.bookverseserver.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/cart/items")
@Tag(name = "Cart Items", description = "🛒 Individual cart item management APIs - Add, update, remove items from shopping cart")
public class CartItemController {

    CartItemService cartItemService;
    SecurityUtils securityUtils;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Add item to cart",
        description = "Add a book listing to the shopping cart. " +
                     "**Behavior**: " +
                     "- If item already exists: Increases quantity " +
                     "- If new item: Adds with specified quantity " +
                     "- Validates stock availability " +
                     "- Updates cart total automatically"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Item added successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid quantity or insufficient stock"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Listing not found or inactive"
        )
    })
    public ApiResponse<CartItemResponse> createCartItem(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Cart item details (listing ID and quantity)",
                required = true,
                content = @Content(schema = @Schema(implementation = CartItemRequest.class))
            )
            @Valid @RequestBody CartItemRequest cartItemRequest,
            Authentication authentication
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        CartItemResponse cartItemResponse = cartItemService.createCartItem(userId, cartItemRequest);
        return ApiResponse.<CartItemResponse>builder()
                .code(1000)
                .message("Cart item created successfully")
                .result(cartItemResponse)
                .build();
    }

    @PutMapping("/{listingId}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Update cart item quantity",
        description = "Change the quantity of an existing cart item. " +
                     "**Special behaviors**: " +
                     "- Set quantity to **0** to remove item " +
                     "- Validates against available stock " +
                     "- Recalculates cart total automatically"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Quantity updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid quantity or exceeds available stock"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Cart item not found"
        )
    })
    public ApiResponse<CartItemResponse> updateCartItem(
            @Parameter(description = "Listing ID of the cart item", example = "1", required = true)
            @PathVariable("listingId") Long listingId,
            
            Authentication authentication,
            
            @Parameter(description = "New quantity (set to 0 to remove)", example = "3", required = true)
            @RequestParam Integer quantity
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        CartItemResponse cartItemResponse = cartItemService.updateCartItem(userId, listingId, quantity);
        return ApiResponse.<CartItemResponse>builder()
                .code(1000)
                .message("Cart item updated successfully")
                .result(cartItemResponse)
                .build();
    }

    @DeleteMapping("/{listingId}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Remove item from cart",
        description = "Permanently remove a specific listing from the shopping cart. " +
                     "Cart total will be recalculated automatically."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Item removed successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Cart item not found"
        )
    })
    public ApiResponse<CartItemResponse> deleteCartItem(
            @Parameter(description = "Listing ID to remove from cart", example = "1", required = true)
            @PathVariable("listingId") Long listingId,
            Authentication authentication
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        CartItemResponse cartItemResponse = cartItemService.deleteCartItem(userId, listingId);
        return ApiResponse.<CartItemResponse>builder()
                .code(1000)
                .message("Cart item deleted successfully")
                .result(cartItemResponse)
                .build();
    }

}
