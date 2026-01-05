package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Wishlist.*;
import com.example.bookverseserver.service.WishlistService;
import com.example.bookverseserver.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlists")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Wishlist", description = "User wishlist/favorites management APIs")
public class WishlistController {

    WishlistService wishlistService;
    SecurityUtils securityUtils;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user's wishlist", description = "Get paginated list of user's favorite listings with price tracking")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Wishlist retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ApiResponse<WishlistResponse> getUserWishlist(
            Authentication authentication,
            Pageable pageable
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        WishlistResponse response = wishlistService.getUserFavorites(userId, pageable);

        return ApiResponse.<WishlistResponse>builder()
                .code(200)
                .message("Wishlist retrieved successfully")
                .result(response)
                .build();
    }

    @PostMapping("/{listingId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add to wishlist", description = "Add a listing to user's wishlist. Idempotent operation.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Added to wishlist successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Listing not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ApiResponse<WishlistItemDTO> addToWishList(
            Authentication authentication,
            @Parameter(description = "Listing ID", example = "123") @PathVariable Long listingId
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        WishlistItemDTO response = wishlistService.addToWishlist(userId, listingId);

        return ApiResponse.<WishlistItemDTO>builder()
                .code(201)
                .message("Added to wishlist successfully")
                .result(response)
                .build();
    }

    @DeleteMapping("/{listingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove from wishlist", description = "Remove a listing from user's wishlist")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Removed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public void removeFromWishList(
            Authentication authentication,
            @Parameter(description = "Listing ID") @PathVariable Long listingId
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        wishlistService.removeFromWishlist(userId, listingId);
    }

    @GetMapping("/check/{listingId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check wishlist status", description = "Check if a listing is in user's wishlist")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Check completed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ApiResponse<WishlistCheckDto> checkWishlistStatus(
            Authentication authentication,
            @Parameter(description = "Listing ID") @PathVariable Long listingId
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);

        return ApiResponse.<WishlistCheckDto>builder()
                .code(200)
                .message("Check completed")
                .result(wishlistService.checkIfInWishlist(userId, listingId))
                .build();
    }

    @GetMapping("/wishlist-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get wishlist count", description = "Get total number of items in user's wishlist")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ApiResponse<WishlistCountDto> getWishlistCount(
            Authentication authentication
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);

        return ApiResponse.<WishlistCountDto>builder()
                .code(200)
                .message("Count retrieved successfully")
                .result(wishlistService.getWishlistCount(userId))
                .build();
    }
}