package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Review.CreateReviewRequest;
import com.example.bookverseserver.dto.request.Review.HideReviewRequest;
import com.example.bookverseserver.dto.request.Review.UpdateReviewRequest;
import com.example.bookverseserver.dto.response.Review.*;
import com.example.bookverseserver.entity.Product.BookMeta;
import com.example.bookverseserver.entity.Product.Review;
import com.example.bookverseserver.entity.Product.ReviewHelpful;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.ReviewMapper;
import com.example.bookverseserver.repository.BookMetaRepository;
import com.example.bookverseserver.repository.ReviewHelpfulRepository;
import com.example.bookverseserver.repository.ReviewRepository;
import com.example.bookverseserver.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReviewService {

  ReviewRepository reviewRepository;
  ReviewHelpfulRepository reviewHelpfulRepository;
  BookMetaRepository bookMetaRepository;
  UserRepository userRepository;
  ReviewMapper reviewMapper;

  // 1. Create Review
  @Transactional
  public ReviewResponse createReview(Long bookId, CreateReviewRequest request, Long userId) {
    // Check if book exists
    BookMeta book = bookMetaRepository.findById(bookId)
        .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));

    // Check if user already reviewed this book
    if (reviewRepository.existsByUserIdAndBookMetaId(userId, bookId)) {
      throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
    }

    // Get user
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    // Create review
    Review review = reviewMapper.toReview(request);
    review.setBookMeta(book);
    review.setUser(user);

    Review savedReview = reviewRepository.save(review);
    log.info("Created review {} for book {} by user {}", savedReview.getId(), bookId, userId);

    return buildReviewResponse(savedReview, userId);
  }

  // 2. Get Reviews by Book (paginated)
  public ReviewsListResponse getReviewsByBookId(Long bookId, int page, int size, String sortBy,
      String sortOrder, Integer rating, Long currentUserId) {
    // Check if book exists
    if (!bookMetaRepository.existsById(bookId)) {
      throw new AppException(ErrorCode.BOOK_NOT_FOUND);
    }

    Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
    Pageable pageable = PageRequest.of(page, size, sort);

    Page<Review> reviewPage;
    if (rating != null) {
      reviewPage = reviewRepository.findByBookMetaIdAndRatingAndIsVisibleTrueAndIsHiddenFalse(bookId, rating, pageable);
    } else {
      reviewPage = reviewRepository.findByBookMetaIdAndIsVisibleTrueAndIsHiddenFalse(bookId, pageable);
    }

    List<ReviewResponse> reviews = reviewPage.getContent().stream()
        .map(review -> buildReviewResponse(review, currentUserId))
        .collect(Collectors.toList());

    return ReviewsListResponse.builder()
        .reviews(reviews)
        .stats(getBookRating(bookId))
        .meta(ReviewsListResponse.PaginationMeta.builder()
            .page(reviewPage.getNumber())
            .totalPages(reviewPage.getTotalPages())
            .totalItems(reviewPage.getTotalElements())
            .itemsPerPage(reviewPage.getSize())
            .build())
        .build();
  }

  // 3. Update Review
  @Transactional
  public ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request, Long userId) {
    Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
        .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

    reviewMapper.updateReview(review, request);
    Review savedReview = reviewRepository.save(review);
    log.info("Updated review {} by user {}", reviewId, userId);

    return buildReviewResponse(savedReview, userId);
  }

  // 4. Delete Review
  @Transactional
  public void deleteReview(Long reviewId, Long userId, boolean isAdmin) {
    Review review;
    if (isAdmin) {
      review = reviewRepository.findById(reviewId)
          .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
    } else {
      review = reviewRepository.findByIdAndUserId(reviewId, userId)
          .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
    }

    reviewRepository.delete(review);
    log.info("Deleted review {} by user {} (isAdmin: {})", reviewId, userId, isAdmin);
  }

  // 5. Get Book Rating Stats
  public ReviewStatsResponse getBookRating(Long bookId) {
    if (!bookMetaRepository.existsById(bookId)) {
      throw new AppException(ErrorCode.BOOK_NOT_FOUND);
    }

    Double avgRating = reviewRepository.findAverageRatingByBookId(bookId);
    long totalReviews = reviewRepository.countByBookMetaIdAndIsVisibleTrueAndIsHiddenFalse(bookId);
    List<Object[]> distribution = reviewRepository.findRatingDistributionByBookId(bookId);

    Map<Integer, Integer> ratingDistribution = new HashMap<>();
    for (int i = 1; i <= 5; i++) {
      ratingDistribution.put(i, 0);
    }
    for (Object[] row : distribution) {
      Integer rating = (Integer) row[0];
      Long count = (Long) row[1];
      ratingDistribution.put(rating, count.intValue());
    }

    Double roundedAvg = avgRating != null
        ? BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP).doubleValue()
        : 0.0;

    return ReviewStatsResponse.builder()
        .bookId(bookId)
        .averageRating(roundedAvg)
        .totalReviews((int) totalReviews)
        .ratingDistribution(ratingDistribution)
        .build();
  }

  // 6. Mark Review as Helpful (toggle)
  @Transactional
  public HelpfulVoteResponse toggleHelpful(Long reviewId, Long userId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

    boolean alreadyVoted = reviewHelpfulRepository.existsByUserIdAndReviewId(userId, reviewId);

    if (alreadyVoted) {
      // Remove vote
      reviewHelpfulRepository.deleteByUserIdAndReviewId(userId, reviewId);
      review.setHelpfulCount(Math.max(0, review.getHelpfulCount() - 1));
    } else {
      // Add vote
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

      ReviewHelpful helpful = ReviewHelpful.builder()
          .user(user)
          .review(review)
          .build();
      reviewHelpfulRepository.save(helpful);
      review.setHelpfulCount(review.getHelpfulCount() + 1);
    }

    reviewRepository.save(review);

    return HelpfulVoteResponse.builder()
        .reviewId(reviewId)
        .helpfulCount(review.getHelpfulCount())
        .userHasVoted(!alreadyVoted)
        .build();
  }

  // 7. Get User's Reviews
  public ReviewsListResponse getUserReviews(Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Review> reviewPage = reviewRepository.findByUserId(userId, pageable);

    List<ReviewResponse> reviews = reviewPage.getContent().stream()
        .map(review -> buildReviewResponse(review, userId))
        .collect(Collectors.toList());

    return ReviewsListResponse.builder()
        .reviews(reviews)
        .stats(null) // No stats needed for user reviews
        .meta(ReviewsListResponse.PaginationMeta.builder()
            .page(reviewPage.getNumber())
            .totalPages(reviewPage.getTotalPages())
            .totalItems(reviewPage.getTotalElements())
            .itemsPerPage(reviewPage.getSize())
            .build())
        .build();
  }

  // 8. Hide Review (Admin)
  @Transactional
  public ReviewResponse hideReview(Long reviewId, HideReviewRequest request) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

    review.setIsHidden(request.getHidden());
    review.setHiddenReason(request.getReason());
    Review savedReview = reviewRepository.save(review);

    log.info("Review {} hidden: {} (reason: {})", reviewId, request.getHidden(), request.getReason());

    return buildReviewResponse(savedReview, null);
  }

  // Helper method to build ReviewResponse with current user context
  private ReviewResponse buildReviewResponse(Review review, Long currentUserId) {
    ReviewResponse response = reviewMapper.toReviewResponse(review);

    if (currentUserId != null) {
      response.setIsCurrentUserReview(review.getUser().getId().equals(currentUserId));
      response.setUserHasVotedHelpful(
          reviewHelpfulRepository.existsByUserIdAndReviewId(currentUserId, review.getId()));
    } else {
      response.setIsCurrentUserReview(false);
      response.setUserHasVotedHelpful(false);
    }

    return response;
  }
}
