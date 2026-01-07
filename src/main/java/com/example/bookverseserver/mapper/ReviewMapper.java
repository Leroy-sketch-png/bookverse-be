package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Review.CreateReviewRequest;
import com.example.bookverseserver.dto.request.Review.UpdateReviewRequest;
import com.example.bookverseserver.dto.response.Review.ReviewResponse;
import com.example.bookverseserver.entity.Product.Review;
import org.mapstruct.*;

/**
 * Mapper for transaction-based reviews.
 * 
 * Reviews are on ORDER ITEMS (verified purchases).
 * They build SELLER trust in the marketplace.
 */
@Mapper(componentModel = "spring")
public interface ReviewMapper {

  /**
   * Map CreateReviewRequest to Review entity.
   * Note: orderItem, listing, seller, user must be set in service.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "orderItem", ignore = true)
  @Mapping(target = "listing", ignore = true)
  @Mapping(target = "seller", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "isVisible", constant = "true")
  @Mapping(target = "isHidden", constant = "false")
  @Mapping(target = "hiddenReason", ignore = true)
  @Mapping(target = "helpfulCount", constant = "0")
  @Mapping(target = "verifiedPurchase", constant = "true") // Always true - only delivered orders can review
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Review toReview(CreateReviewRequest request);

  /**
   * Update existing review from request.
   */
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "orderItem", ignore = true)
  @Mapping(target = "listing", ignore = true)
  @Mapping(target = "seller", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "isVisible", ignore = true)
  @Mapping(target = "isHidden", ignore = true)
  @Mapping(target = "hiddenReason", ignore = true)
  @Mapping(target = "helpfulCount", ignore = true)
  @Mapping(target = "verifiedPurchase", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateReview(@MappingTarget Review review, UpdateReviewRequest request);

  /**
   * Map Review entity to ReviewResponse.
   */
  @Mapping(target = "listingId", source = "review.listing.id")
  @Mapping(target = "sellerId", source = "review.seller.id")
  @Mapping(target = "reviewerName", expression = "java(getReviewerName(review))")
  @Mapping(target = "reviewerInitials", expression = "java(getReviewerInitials(review))")
  @Mapping(target = "reviewerAvatarUrl", expression = "java(getReviewerAvatar(review))")
  @Mapping(target = "isCurrentUserReview", ignore = true)
  @Mapping(target = "userHasVotedHelpful", ignore = true)
  ReviewResponse toReviewResponse(Review review);

  /**
   * Get reviewer display name (first name + last initial for privacy).
   */
  default String getReviewerName(Review review) {
    if (review.getUser() == null) return "Anonymous";
    
    // Try to get display name from profile
    if (review.getUser().getUserProfile() != null) {
      String fullName = review.getUser().getUserProfile().getDisplayName();
      if (fullName != null && !fullName.isBlank()) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
          return parts[0] + " " + parts[parts.length - 1].charAt(0) + ".";
        }
        return parts[0];
      }
    }
    
    // Fallback to username
    String username = review.getUser().getUsername();
    if (username != null && !username.isBlank()) {
      return username.length() > 10 ? username.substring(0, 10) + "..." : username;
    }
    
    return "Anonymous";
  }

  /**
   * Get reviewer initials for avatar placeholder.
   */
  default String getReviewerInitials(Review review) {
    if (review.getUser() == null) return "?";
    
    // Try to get from display name
    if (review.getUser().getUserProfile() != null) {
      String fullName = review.getUser().getUserProfile().getDisplayName();
      if (fullName != null && !fullName.isBlank()) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
          return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return ("" + parts[0].charAt(0)).toUpperCase();
      }
    }
    
    // Fallback to username
    String username = review.getUser().getUsername();
    if (username != null && !username.isBlank()) {
      return ("" + username.charAt(0)).toUpperCase();
    }
    
    return "?";
  }

  /**
   * Get reviewer avatar URL.
   */
  default String getReviewerAvatar(Review review) {
    if (review.getUser() != null && review.getUser().getUserProfile() != null) {
      return review.getUser().getUserProfile().getAvatarUrl();
    }
    return null;
  }
}
