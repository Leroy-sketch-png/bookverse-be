package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Review.CreateReviewRequest;
import com.example.bookverseserver.dto.request.Review.HideReviewRequest;
import com.example.bookverseserver.dto.request.Review.UpdateReviewRequest;
import com.example.bookverseserver.dto.response.Review.*;
import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.Order_Payment.OrderItem;
import com.example.bookverseserver.entity.Product.Review;
import com.example.bookverseserver.entity.Product.ReviewHelpful;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.entity.User.UserProfile;
import com.example.bookverseserver.enums.OrderStatus;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.OrderItemRepository;
import com.example.bookverseserver.repository.ReviewHelpfulRepository;
import com.example.bookverseserver.repository.ReviewRepository;
import com.example.bookverseserver.repository.UserRepository;
import com.example.bookverseserver.util.HtmlSanitizer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
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

/**
 * Service for transaction-based reviews.
 * 
 * MARKETPLACE MODEL:
 * - Reviews are on ORDER ITEMS (verified purchases only)
 * - One review per order item (unique constraint)
 * - Reviews build SELLER trust, not book ratings
 * - Buyer reviews: "Did the seller deliver as promised?"
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReviewService {

    ReviewRepository reviewRepository;
    ReviewHelpfulRepository reviewHelpfulRepository;
    OrderItemRepository orderItemRepository;
    UserRepository userRepository;
    HtmlSanitizer htmlSanitizer;

    // =========================================================================
    // 1. Create Review (for completed order item)
    // =========================================================================
    
    /**
     * Create a review for a delivered order item.
     * 
     * @param orderId The order ID
     * @param orderItemId The specific order item ID being reviewed
     * @param request The review content (rating, comment)
     * @param userId The authenticated user ID
     */
    @Transactional
    public ReviewResponse createReview(Long orderId, Long orderItemId, CreateReviewRequest request, Long userId) {
        // 1. Get the order item
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND));
        
        // 2. Verify order ownership
        Order order = orderItem.getOrder();
        if (!order.getId().equals(orderId)) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!order.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        // 3. Verify order is delivered
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new AppException(ErrorCode.ORDER_NOT_DELIVERED);
        }
        
        // 4. Check if already reviewed
        if (reviewRepository.existsByOrderItemId(orderItemId)) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
        
        // 5. Get buyer
        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // 6. Create review with XSS-sanitized comment
        String sanitizedComment = htmlSanitizer.sanitizeBasic(request.getComment());
        
        Review review = Review.builder()
                .orderItem(orderItem)
                .listing(orderItem.getListing())
                .seller(orderItem.getSeller())
                .user(buyer)
                .rating(request.getRating())
                .comment(sanitizedComment)
                .verifiedPurchase(true) // Always true - we verified the order
                .build();
        
        Review savedReview = reviewRepository.save(review);
        log.info("Created review {} for order item {} by user {}", 
                savedReview.getId(), orderItemId, userId);
        
        return buildReviewResponse(savedReview, userId);
    }

    // =========================================================================
    // 2. Get Reviews for a Listing (product page)
    // =========================================================================
    
    public ReviewsListResponse getReviewsByListingId(Long listingId, int page, int size, 
            String sortBy, String sortOrder, Integer rating, Long currentUserId) {
        
        Sort sort = determineSortOrder(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Review> reviewPage;
        if (rating != null) {
            reviewPage = reviewRepository.findByListingIdAndRatingAndIsVisibleTrueAndIsHiddenFalse(
                    listingId, rating, pageable);
        } else {
            reviewPage = reviewRepository.findByListingIdAndIsVisibleTrueAndIsHiddenFalse(listingId, pageable);
        }
        
        List<ReviewResponse> reviews = reviewPage.getContent().stream()
                .map(r -> buildReviewResponse(r, currentUserId))
                .collect(Collectors.toList());
        
        return ReviewsListResponse.builder()
                .reviews(reviews)
                .stats(getListingReviewStats(listingId))
                .meta(buildPaginationMeta(reviewPage))
                .build();
    }

    // =========================================================================
    // 3. Get Reviews for a Seller (seller profile)
    // =========================================================================
    
    public ReviewsListResponse getReviewsBySellerId(Long sellerId, int page, int size, 
            String sortBy, String sortOrder, Integer rating, Long currentUserId) {
        
        Sort sort = determineSortOrder(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Review> reviewPage;
        if (rating != null) {
            reviewPage = reviewRepository.findBySellerIdAndRatingAndIsVisibleTrueAndIsHiddenFalse(
                    sellerId, rating, pageable);
        } else {
            reviewPage = reviewRepository.findBySellerIdAndIsVisibleTrueAndIsHiddenFalse(sellerId, pageable);
        }
        
        List<ReviewResponse> reviews = reviewPage.getContent().stream()
                .map(r -> buildReviewResponse(r, currentUserId))
                .collect(Collectors.toList());
        
        return ReviewsListResponse.builder()
                .reviews(reviews)
                .stats(getSellerReviewStats(sellerId))
                .meta(buildPaginationMeta(reviewPage))
                .build();
    }

    // =========================================================================
    // 4. Get Listing Review Stats
    // =========================================================================
    
    public ReviewStatsResponse getListingReviewStats(Long listingId) {
        Double avgRating = reviewRepository.findAverageRatingByListingId(listingId);
        long totalReviews = reviewRepository.countByListingIdAndIsVisibleTrueAndIsHiddenFalse(listingId);
        List<Object[]> distribution = reviewRepository.findRatingDistributionByListingId(listingId);
        
        return buildStatsResponse(listingId, avgRating, totalReviews, distribution);
    }

    // =========================================================================
    // 5. Get Seller Review Stats (for seller reputation)
    // =========================================================================
    
    public ReviewStatsResponse getSellerReviewStats(Long sellerId) {
        Double avgRating = reviewRepository.calculateAverageRatingForSeller(sellerId);
        long totalReviews = reviewRepository.countBySellerIdAndIsVisibleTrueAndIsHiddenFalse(sellerId);
        List<Object[]> distribution = reviewRepository.getRatingDistributionForSeller(sellerId);
        
        return buildStatsResponse(sellerId, avgRating, totalReviews, distribution);
    }

    // =========================================================================
    // 6. Update Review
    // =========================================================================
    
    @Transactional
    public ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request, Long userId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        
        // 30-day limit per Vision spec - reviews can only be edited within 30 days of creation
        if (review.getCreatedAt().plusDays(30).isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.REVIEW_UPDATE_EXPIRED);
        }
        
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            // Sanitize user input to prevent XSS
            review.setComment(htmlSanitizer.sanitizeBasic(request.getComment()));
        }
        
        Review savedReview = reviewRepository.save(review);
        log.info("Updated review {} by user {}", reviewId, userId);
        
        return buildReviewResponse(savedReview, userId);
    }

    // =========================================================================
    // 7. Delete Review
    // =========================================================================
    
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
        
        // Delete associated helpful votes first
        reviewHelpfulRepository.deleteByReviewId(reviewId);
        
        reviewRepository.delete(review);
        log.info("Deleted review {} by user {} (isAdmin: {})", reviewId, userId, isAdmin);
    }

    // =========================================================================
    // 8. Toggle Helpful Vote
    // =========================================================================
    
    @Transactional
    public HelpfulVoteResponse toggleHelpful(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        
        // Cannot vote on own review
        if (review.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.CANNOT_VOTE_OWN_REVIEW);
        }
        
        boolean alreadyVoted = reviewHelpfulRepository.existsByUserIdAndReviewId(userId, reviewId);
        
        if (alreadyVoted) {
            reviewHelpfulRepository.deleteByUserIdAndReviewId(userId, reviewId);
            review.setHelpfulCount(Math.max(0, review.getHelpfulCount() - 1));
        } else {
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

    // =========================================================================
    // 9. Get User's Reviews
    // =========================================================================
    
    public ReviewsListResponse getUserReviews(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviewPage = reviewRepository.findByUserId(userId, pageable);
        
        List<ReviewResponse> reviews = reviewPage.getContent().stream()
                .map(r -> buildReviewResponse(r, userId))
                .collect(Collectors.toList());
        
        return ReviewsListResponse.builder()
                .reviews(reviews)
                .stats(null)
                .meta(buildPaginationMeta(reviewPage))
                .build();
    }

    // =========================================================================
    // 10. Hide Review (Admin/Moderation)
    // =========================================================================
    
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

    // =========================================================================
    // Helper Methods
    // =========================================================================
    
    private ReviewResponse buildReviewResponse(Review review, Long currentUserId) {
        User reviewer = review.getUser();
        UserProfile profile = reviewer.getUserProfile();
        
        String reviewerName = "Anonymous";
        String reviewerInitials = "A";
        
        if (profile != null && profile.getFullName() != null) {
            reviewerName = profile.getFullName();
            reviewerInitials = generateInitials(profile.getFullName());
        } else if (reviewer.getUsername() != null) {
            reviewerName = reviewer.getUsername();
            reviewerInitials = reviewer.getUsername().substring(0, 1).toUpperCase();
        }
        
        ReviewResponse response = ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .verifiedPurchase(review.getVerifiedPurchase())
                .helpfulCount(review.getHelpfulCount())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .reviewerName(reviewerName)
                .reviewerInitials(reviewerInitials)
                .reviewerAvatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .listingId(review.getListing().getId())
                .sellerId(review.getSeller().getId())
                .build();
        
        if (currentUserId != null) {
            response.setIsCurrentUserReview(reviewer.getId().equals(currentUserId));
            response.setUserHasVotedHelpful(
                    reviewHelpfulRepository.existsByUserIdAndReviewId(currentUserId, review.getId()));
        } else {
            response.setIsCurrentUserReview(false);
            response.setUserHasVotedHelpful(false);
        }
        
        return response;
    }
    
    private ReviewStatsResponse buildStatsResponse(Long entityId, Double avgRating, 
            long totalReviews, List<Object[]> distribution) {
        
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
                .bookId(entityId) // Legacy field name, could be listingId or sellerId
                .averageRating(roundedAvg)
                .totalReviews((int) totalReviews)
                .ratingDistribution(ratingDistribution)
                .build();
    }
    
    private ReviewsListResponse.PaginationMeta buildPaginationMeta(Page<?> page) {
        return ReviewsListResponse.PaginationMeta.builder()
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .itemsPerPage(page.getSize())
                .build();
    }
    
    private Sort determineSortOrder(String sortBy, String sortOrder) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        String sortField = switch (sortBy) {
            case "helpful" -> "helpfulCount";
            case "highest" -> "rating";
            case "lowest" -> "rating";
            case "newest" -> "createdAt";
            default -> "createdAt";
        };
        
        // For "lowest", we want ASC; for "highest", we want DESC
        if ("lowest".equals(sortBy)) {
            direction = Sort.Direction.ASC;
        } else if ("highest".equals(sortBy)) {
            direction = Sort.Direction.DESC;
        }
        
        return Sort.by(direction, sortField);
    }
    
    private String generateInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "?";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
        return parts[0].substring(0, 1).toUpperCase();
    }
}
