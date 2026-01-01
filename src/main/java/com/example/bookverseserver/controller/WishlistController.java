package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Wishlist.WishlistItemDTO;
import com.example.bookverseserver.dto.response.Wishlist.WishlistResponse;
import com.example.bookverseserver.service.WishlistService;
import com.example.bookverseserver.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlists")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WishlistController {

    WishlistService wishlistService;
    SecurityUtils securityUtils;

    @GetMapping
    public ApiResponse<WishlistResponse> getUserWishlist(
            Authentication authentication,
            Pageable pageable

    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        WishlistResponse response = wishlistService.getUserFavorites(userId, pageable);

        return ApiResponse.<WishlistResponse>builder()
                .code(200)
                .result(response)
                .build();
    }

    @PostMapping("/{listingId}")
    public ApiResponse<WishlistItemDTO> addToWishList(
            Authentication authentication,
            @PathVariable Long listingId
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        WishlistItemDTO response = wishlistService.addToWishlist(userId, listingId);

        return ApiResponse.<WishlistItemDTO>builder()
                .code(200)
                .result(response)
                .build();
    }

    @DeleteMapping("/{listingId}")
    public ApiResponse removeFromWishList(
            Authentication authentication,
            @PathVariable Long listingId
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        wishlistService.removeFromWishlist(userId, listingId);

        return ApiResponse.builder()
                .code(200)
                .result("Item removed from wishlist.")
                .build();
    }
}
