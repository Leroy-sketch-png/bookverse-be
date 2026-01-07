package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Seller.SellerProfileListingResponse;
import com.example.bookverseserver.dto.response.Seller.SellerProfileReviewsWrapper;
import com.example.bookverseserver.dto.response.User.SellerProfileResponse;
import com.example.bookverseserver.service.PublicSellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public Seller Profile API - per Vision API_CONTRACTS.md §7.4 Public Seller Profile.
 * 
 * These endpoints are PUBLIC (no authentication required) and allow
 * buyers to view a seller's storefront, listings, and reviews.
 */
@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Public Seller Profile", description = "Public APIs for viewing seller storefronts")
public class PublicSellerController {

    PublicSellerService publicSellerService;

    @GetMapping("/{sellerSlug}/profile")
    @Operation(summary = "Get seller's public profile", 
               description = "Returns public profile information for a seller (no auth required)")
    public ApiResponse<SellerProfileResponse> getSellerProfile(
            @PathVariable String sellerSlug) {
        log.debug("Fetching public profile for seller: {}", sellerSlug);
        return ApiResponse.<SellerProfileResponse>builder()
                .message("Seller profile retrieved successfully")
                .result(publicSellerService.getSellerProfile(sellerSlug))
                .build();
    }

    @GetMapping("/{sellerSlug}/profile/listings")
    @Operation(summary = "Get seller's public listings", 
               description = "Returns active listings for a seller (no auth required)")
    public ApiResponse<List<SellerProfileListingResponse>> getSellerListings(
            @PathVariable String sellerSlug,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        log.debug("Fetching listings for seller: {}", sellerSlug);
        return ApiResponse.<List<SellerProfileListingResponse>>builder()
                .message("Seller listings retrieved successfully")
                .result(publicSellerService.getSellerListings(sellerSlug, page, limit, category, sortBy))
                .build();
    }

    @GetMapping("/{sellerSlug}/profile/reviews")
    @Operation(summary = "Get seller's reviews", 
               description = "Returns reviews for a seller with stats (no auth required)")
    public ApiResponse<SellerProfileReviewsWrapper> getSellerReviews(
            @PathVariable String sellerSlug,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("Fetching reviews for seller: {}", sellerSlug);
        return ApiResponse.<SellerProfileReviewsWrapper>builder()
                .message("Seller reviews retrieved successfully")
                .result(publicSellerService.getSellerReviews(sellerSlug, page, limit))
                .build();
    }
}
