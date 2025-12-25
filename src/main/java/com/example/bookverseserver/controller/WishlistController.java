package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Wishlist.WishlistResponse;
import com.example.bookverseserver.service.WishlistService;
import com.example.bookverseserver.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
