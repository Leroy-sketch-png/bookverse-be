package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Review.CreateReviewRequest;
import com.example.bookverseserver.dto.request.Review.HideReviewRequest;
import com.example.bookverseserver.dto.request.Review.UpdateReviewRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Review.*;
import com.example.bookverseserver.service.ReviewService;
import com.example.bookverseserver.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Review Controller - Transaction-Based Reviews.
 * 
 * MARKETPLACE MODEL:
 * - Reviews are created on ORDER ITEMS (verified purchases)
 * - Reviews are displayed on LISTINGS and SELLER PROFILES
 * - Reviews build SELLER trust, not book ratings
 * 
 * Per Vision API_CONTRACTS.md: POST /orders/:orderId/items/:itemId/review
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Reviews", description = "Transaction-based reviews for marketplace trust")
public class ReviewController {

    private final ReviewService reviewService;
    private final SecurityUtils securityUtils;

    // =========================================================================
    // CREATE REVIEW (on Order Item)
    // =========================================================================

    @PostMapping("/orders/{orderId}/items/{itemId}/review")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create review for order item", 
               description = "Creates a review for a delivered order item. Only the buyer can review.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Review created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Order not delivered"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not order owner"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Already reviewed")
    })
    public ApiResponse<ReviewResponse> createReview(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Parameter(description = "Order Item ID") @PathVariable Long itemId,
            @RequestBody @Valid CreateReviewRequest request,
            Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        return ApiResponse.<ReviewResponse>builder()
                .message("Review created successfully")
                .result(reviewService.createReview(orderId, itemId, request, userId))
                .build();
    }

    // =========================================================================
    // GET REVIEWS FOR LISTING (product page)
    // =========================================================================

    @GetMapping("/listings/{listingId}/reviews")
    @Operation(summary = "Get reviews for a listing", 
               description = "Returns paginated reviews for a specific listing (product)")
    public ApiResponse<ReviewsListResponse> getListingReviews(
            @Parameter(description = "Listing ID") @PathVariable Long listingId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) Integer rating,
            Authentication authentication) {
        Long currentUserId = authentication != null ? securityUtils.getCurrentUserId(authentication) : null;
        // Convert 1-indexed (API) to 0-indexed (Spring)
        int pageIndex = Math.max(0, page - 1);
        return ApiResponse.<ReviewsListResponse>builder()
                .message("Reviews retrieved successfully")
                .result(reviewService.getReviewsByListingId(listingId, pageIndex, size, sortBy, sortOrder, rating, currentUserId))
                .build();
    }

    // =========================================================================
    // GET REVIEWS FOR SELLER (seller profile reputation)
    // =========================================================================

    @GetMapping("/sellers/{sellerId}/reviews")
    @Operation(summary = "Get reviews for a seller", 
               description = "Returns all reviews for a seller's listings (seller reputation)")
    public ApiResponse<ReviewsListResponse> getSellerReviews(
            @Parameter(description = "Seller ID") @PathVariable Long sellerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) Integer rating,
            Authentication authentication) {
        Long currentUserId = authentication != null ? securityUtils.getCurrentUserId(authentication) : null;
        // Convert 1-indexed (API) to 0-indexed (Spring)
        int pageIndex = Math.max(0, page - 1);
        return ApiResponse.<ReviewsListResponse>builder()
                .message("Seller reviews retrieved successfully")
                .result(reviewService.getReviewsBySellerId(sellerId, pageIndex, size, sortBy, sortOrder, rating, currentUserId))
                .build();
    }

    // =========================================================================
    // GET LISTING RATING STATS
    // =========================================================================

    @GetMapping("/listings/{listingId}/rating")
    @Operation(summary = "Get listing rating statistics", 
               description = "Returns average rating, count, and distribution for a listing")
    public ApiResponse<ReviewStatsResponse> getListingRating(
            @Parameter(description = "Listing ID") @PathVariable Long listingId) {
        return ApiResponse.<ReviewStatsResponse>builder()
                .message("Rating statistics retrieved")
                .result(reviewService.getListingReviewStats(listingId))
                .build();
    }

    // =========================================================================
    // GET SELLER RATING STATS (seller reputation)
    // =========================================================================

    @GetMapping("/sellers/{sellerId}/rating")
    @Operation(summary = "Get seller rating statistics", 
               description = "Returns seller reputation: average rating across all their listings")
    public ApiResponse<ReviewStatsResponse> getSellerRating(
            @Parameter(description = "Seller ID") @PathVariable Long sellerId) {
        return ApiResponse.<ReviewStatsResponse>builder()
                .message("Seller rating retrieved")
                .result(reviewService.getSellerReviewStats(sellerId))
                .build();
    }

    // =========================================================================
    // UPDATE REVIEW
    // =========================================================================

    @PatchMapping("/reviews/{reviewId}")
    @Operation(summary = "Update own review", 
               description = "Updates a review. Only the author can update within 30 days.")
    public ApiResponse<ReviewResponse> updateReview(
            @Parameter(description = "Review ID") @PathVariable Long reviewId,
            @RequestBody @Valid UpdateReviewRequest request,
            Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        return ApiResponse.<ReviewResponse>builder()
                .message("Review updated successfully")
                .result(reviewService.updateReview(reviewId, request, userId))
                .build();
    }

    // =========================================================================
    // DELETE REVIEW
    // =========================================================================

    @DeleteMapping("/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete own review", 
               description = "Deletes a review. Author or admin can delete.")
    public ApiResponse<Void> deleteReview(
            @Parameter(description = "Review ID") @PathVariable Long reviewId,
            Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        reviewService.deleteReview(reviewId, userId, isAdmin);
        return ApiResponse.<Void>builder()
                .message("Review deleted successfully")
                .build();
    }

    // =========================================================================
    // TOGGLE HELPFUL VOTE
    // =========================================================================

    @PostMapping("/reviews/{reviewId}/helpful")
    @Operation(summary = "Toggle helpful vote", 
               description = "Marks or unmarks a review as helpful. Cannot vote on own reviews.")
    public ApiResponse<HelpfulVoteResponse> toggleHelpful(
            @Parameter(description = "Review ID") @PathVariable Long reviewId,
            Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        return ApiResponse.<HelpfulVoteResponse>builder()
                .message("Helpful vote toggled")
                .result(reviewService.toggleHelpful(reviewId, userId))
                .build();
    }

    // =========================================================================
    // GET MY REVIEWS
    // =========================================================================

    @GetMapping("/users/me/reviews")
    @Operation(summary = "Get my reviews", 
               description = "Returns all reviews created by the current user")
    public ApiResponse<ReviewsListResponse> getMyReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        // Convert 1-indexed (API) to 0-indexed (Spring)
        int pageIndex = Math.max(0, page - 1);
        return ApiResponse.<ReviewsListResponse>builder()
                .message("User reviews retrieved")
                .result(reviewService.getUserReviews(userId, pageIndex, size))
                .build();
    }

    // =========================================================================
    // ADMIN: HIDE REVIEW
    // =========================================================================

    @PatchMapping("/admin/reviews/{reviewId}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Hide/Unhide review (Admin)", 
               description = "Admin can hide inappropriate reviews with a reason")
    public ApiResponse<ReviewResponse> hideReview(
            @Parameter(description = "Review ID") @PathVariable Long reviewId,
            @RequestBody @Valid HideReviewRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .message("Review visibility updated")
                .result(reviewService.hideReview(reviewId, request))
                .build();
    }
}
