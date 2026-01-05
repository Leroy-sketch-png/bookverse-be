package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Review.CreateReviewRequest;
import com.example.bookverseserver.dto.request.Review.UpdateReviewRequest;
import com.example.bookverseserver.dto.response.Review.ReviewResponse;
import com.example.bookverseserver.entity.Product.Review;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "bookMeta", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "isVisible", constant = "true")
  @Mapping(target = "isHidden", constant = "false")
  @Mapping(target = "hiddenReason", ignore = true)
  @Mapping(target = "helpfulCount", constant = "0")
  @Mapping(target = "verifiedPurchase", constant = "false")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Review toReview(CreateReviewRequest request);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "bookMeta", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "isVisible", ignore = true)
  @Mapping(target = "isHidden", ignore = true)
  @Mapping(target = "hiddenReason", ignore = true)
  @Mapping(target = "helpfulCount", ignore = true)
  @Mapping(target = "verifiedPurchase", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateReview(@MappingTarget Review review, UpdateReviewRequest request);

  @Mapping(target = "id", expression = "java(String.valueOf(review.getId()))")
  @Mapping(target = "bookId", expression = "java(String.valueOf(review.getBookMeta().getId()))")
  @Mapping(target = "userId", source = "review.user.id")
  @Mapping(target = "userName", source = "review.user.username")
  @Mapping(target = "userAvatar", expression = "java(getUserAvatar(review))")
  @Mapping(target = "helpful", source = "review.helpfulCount")
  @Mapping(target = "verified", source = "review.verifiedPurchase")
  @Mapping(target = "isCurrentUserReview", ignore = true)
  @Mapping(target = "userHasVotedHelpful", ignore = true)
  ReviewResponse toReviewResponse(Review review);

  default String getUserAvatar(Review review) {
    if (review.getUser() != null && review.getUser().getUserProfile() != null) {
      return review.getUser().getUserProfile().getAvatarUrl();
    }
    return null;
  }
}
