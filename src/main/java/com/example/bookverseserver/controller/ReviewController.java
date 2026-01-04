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

@RestController
@RequiredArgsConstructor
@Tag(name = "Review", description = "Review & Rating APIs - Users can review books and vote helpful")
public class ReviewController {

  private final ReviewService reviewService;
  private final SecurityUtils securityUtils;

  @PostMapping("/books/{bookId}/reviews")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a review for a book", description = "Creates a new review for a book. Each user can only review a book once.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Review created successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Book not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already reviewed this book")
  })
  public ApiResponse<ReviewResponse> createReview(
      @Parameter(description = "Book ID", example = "1") @PathVariable Long bookId,
      @RequestBody @Valid CreateReviewRequest request,
      Authentication authentication) {
    Long userId = securityUtils.getCurrentUserId(authentication);
    return ApiResponse.<ReviewResponse>builder()
        .message("Review created successfully")
        .result(reviewService.createReview(bookId, request, userId))
        .build();
  }

  @GetMapping("/books/{bookId}/reviews")
  @Operation(summary = "Get reviews for a book", description = "Retrieves paginated list of reviews for a book with optional rating filter")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Book not found")
  })
  public ApiResponse<ReviewsListResponse> getReviewsByBook(
      @Parameter(description = "Book ID", example = "1") @PathVariable Long bookId,
      @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
      @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
      @Parameter(description = "Sort direction", example = "desc") @RequestParam(defaultValue = "desc") String sortOrder,
      @Parameter(description = "Filter by rating (1-5)", example = "5") @RequestParam(required = false) Integer rating,
      Authentication authentication) {
    Long currentUserId = authentication != null ? securityUtils.getCurrentUserId(authentication) : null;
    return ApiResponse.<ReviewsListResponse>builder()
        .message("Reviews retrieved successfully")
        .result(reviewService.getReviewsByBookId(bookId, page, size, sortBy, sortOrder, rating, currentUserId))
        .build();
  }

  @PutMapping("/reviews/{id}")
  @Operation(summary = "Update a review", description = "Updates an existing review. Only the review owner can update.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review updated successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found or not owned by user")
  })
  public ApiResponse<ReviewResponse> updateReview(
      @Parameter(description = "Review ID", example = "1") @PathVariable Long id,
      @RequestBody @Valid UpdateReviewRequest request,
      Authentication authentication) {
    Long userId = securityUtils.getCurrentUserId(authentication);
    return ApiResponse.<ReviewResponse>builder()
        .message("Review updated successfully")
        .result(reviewService.updateReview(id, request, userId))
        .build();
  }

  @DeleteMapping("/reviews/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a review", description = "Deletes a review. Owners can delete their own reviews, admins can delete any.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Review deleted successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found")
  })
  public ApiResponse<Void> deleteReview(
      @Parameter(description = "Review ID", example = "1") @PathVariable Long id,
      Authentication authentication) {
    Long userId = securityUtils.getCurrentUserId(authentication);
    boolean isAdmin = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    reviewService.deleteReview(id, userId, isAdmin);
    return ApiResponse.<Void>builder()
        .message("Review deleted successfully")
        .build();
  }

  @GetMapping("/books/{bookId}/rating")
  @Operation(summary = "Get book rating statistics", description = "Returns average rating, total reviews, and rating distribution for a book")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rating statistics retrieved"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Book not found")
  })
  public ApiResponse<ReviewStatsResponse> getBookRating(
      @Parameter(description = "Book ID", example = "1") @PathVariable Long bookId) {
    return ApiResponse.<ReviewStatsResponse>builder()
        .message("Book rating retrieved successfully")
        .result(reviewService.getBookRating(bookId))
        .build();
  }

  @PostMapping("/reviews/{id}/helpful")
  @Operation(summary = "Toggle helpful vote", description = "Marks or unmarks a review as helpful. Toggles the vote if already voted.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vote toggled successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found")
  })
  public ApiResponse<HelpfulVoteResponse> toggleHelpful(
      @Parameter(description = "Review ID", example = "1") @PathVariable Long id,
      Authentication authentication) {
    Long userId = securityUtils.getCurrentUserId(authentication);
    return ApiResponse.<HelpfulVoteResponse>builder()
        .message("Helpful vote toggled successfully")
        .result(reviewService.toggleHelpful(id, userId))
        .build();
  }

  @GetMapping("/users/me/reviews")
  @Operation(summary = "Get my reviews", description = "Retrieves paginated list of reviews created by the current user")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User reviews retrieved successfully")
  })
  public ApiResponse<ReviewsListResponse> getMyReviews(
      @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
      Authentication authentication) {
    Long userId = securityUtils.getCurrentUserId(authentication);
    return ApiResponse.<ReviewsListResponse>builder()
        .message("User reviews retrieved successfully")
        .result(reviewService.getUserReviews(userId, page, size))
        .build();
  }

  @PatchMapping("/admin/reviews/{id}/hide")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Hide/Unhide a review (Admin)", description = "Allows admin to hide or unhide a review with a reason. Hidden reviews won't appear in public listings.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review visibility updated"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found")
  })
  public ApiResponse<ReviewResponse> hideReview(
      @Parameter(description = "Review ID", example = "1") @PathVariable Long id,
      @RequestBody @Valid HideReviewRequest request) {
    return ApiResponse.<ReviewResponse>builder()
        .message("Review visibility updated successfully")
        .result(reviewService.hideReview(id, request))
        .build();
  }
}
