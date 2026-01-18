package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.Seller.SellerProfileListingResponse;
import com.example.bookverseserver.dto.response.Seller.SellerProfileReviewResponse;
import com.example.bookverseserver.dto.response.Seller.SellerProfileReviewsWrapper;
import com.example.bookverseserver.dto.response.User.SellerProfileResponse;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.Product.Review;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.entity.User.UserProfile;
import com.example.bookverseserver.enums.ListingStatus;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.ListingRepository;
import com.example.bookverseserver.repository.OrderItemRepository;
import com.example.bookverseserver.repository.ReviewRepository;
import com.example.bookverseserver.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Public Seller Service - handles public-facing seller profile operations.
 * 
 * These operations do NOT require authentication and allow buyers to
 * browse seller storefronts.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional(readOnly = true)
public class PublicSellerService {

    UserRepository userRepository;
    ListingRepository listingRepository;
    ReviewRepository reviewRepository;
    OrderItemRepository orderItemRepository;
    SellerStatsService sellerStatsService;

    /**
     * Get a seller's public profile by username/slug.
     */
    public SellerProfileResponse getSellerProfile(String sellerSlug) {
        User seller = userRepository.findByUsername(sellerSlug)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserProfile profile = seller.getUserProfile();
        
        // Calculate stats
        Long totalSales = listingRepository.countBySellerId(seller.getId());
        Double averageRating = calculateSellerAverageRating(seller.getId());
        Integer totalReviews = countSellerReviews(seller.getId());
        
        // Calculate membership duration
        LocalDate memberSince = seller.getCreatedAt() != null 
                ? seller.getCreatedAt().toLocalDate() 
                : LocalDate.now();
        String membershipDuration = formatMembershipDuration(memberSince);
        
        // Calculate repeat buyer rate
        long totalBuyers = orderItemRepository.countDistinctBuyersBySellerId(seller.getId());
        long repeatBuyers = orderItemRepository.countRepeatBuyersBySellerId(seller.getId());
        double repeatBuyerRate = totalBuyers > 0 ? (repeatBuyers * 100.0 / totalBuyers) : 0.0;

        // Get calculated stats from actual order data
        SellerStatsService.SellerStats calculatedStats = sellerStatsService.getSellerStats(seller.getId());
        boolean statsVerified = calculatedStats.isVerified();
        
        // Auto-derive seller specialty tags from their top 3 listing categories
        List<String> topCategories = listingRepository.findTopCategoryNamesBySellerId(seller.getId());
        List<String> sellerTags = topCategories.stream().limit(3).toList();
        
        // Use calculated values if available, otherwise fall back to self-declared
        Double fulfillmentRate = calculatedStats.fulfillmentRate() != null 
                ? calculatedStats.fulfillmentRate().doubleValue()
                : (profile != null && profile.getFulfillmentRate() != null 
                        ? profile.getFulfillmentRate().doubleValue() : 100.0);
        
        String responseTime = calculatedStats.responseTime() != null 
                ? calculatedStats.responseTime()
                : (profile != null && profile.getResponseTime() != null 
                        ? profile.getResponseTime() : "< 1 hour");

        SellerProfileResponse.SellerStats stats = SellerProfileResponse.SellerStats.builder()
                .totalSales(totalSales.intValue())
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalReviews(totalReviews)
                .fulfillmentRate(fulfillmentRate)
                .responseTime(responseTime)
                .repeatBuyerRate(repeatBuyerRate)
                .membershipDuration(membershipDuration)
                .statsVerified(statsVerified)
                .build();

        return SellerProfileResponse.builder()
                .id(seller.getId())
                .username(seller.getUsername())
                .displayName(profile != null && profile.getFullName() != null 
                        ? profile.getFullName() : seller.getUsername())
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .coverImageUrl(profile != null ? profile.getCoverImageUrl() : null)
                .bio(profile != null ? profile.getBio() : null)
                .location(profile != null ? profile.getLocation() : null)
                .memberSince(memberSince)
                .isVerified(profile != null && profile.getIsProSeller() != null 
                        ? profile.getIsProSeller() : false)
                .isProSeller(profile != null && profile.getIsProSeller() != null 
                        ? profile.getIsProSeller() : false)
                .badge(determineBadge(totalSales.intValue(), averageRating))
                .stats(stats)
                .tags(sellerTags)
                .build();
    }

    /**
     * Get a seller's active listings.
     */
    public List<SellerProfileListingResponse> getSellerListings(
            String sellerSlug, int page, int limit, String category, String sortBy) {
        
        User seller = userRepository.findByUsername(sellerSlug)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Sort sort = determineSortOrder(sortBy);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit, sort);

        List<Listing> listings;
        if (category != null && !category.isEmpty()) {
            listings = listingRepository.findBySellerIdAndStatusAndCategorySlug(
                    seller.getId(), ListingStatus.ACTIVE, category, pageable);
        } else {
            listings = listingRepository.findBySellerIdAndStatus(
                    seller.getId(), ListingStatus.ACTIVE, pageable).getContent();
        }

        return listings.stream()
                .map(this::mapToListingResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get reviews for a seller's listings.
     */
    public SellerProfileReviewsWrapper getSellerReviews(String sellerSlug, int page, int limit) {
        User seller = userRepository.findByUsername(sellerSlug)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit, 
                Sort.by(Sort.Direction.DESC, "createdAt"));

        // Get reviews for listings owned by this seller
        List<Review> reviews = reviewRepository.findByListingSellerId(seller.getId(), pageable);
        
        // Calculate stats
        Double averageRating = calculateSellerAverageRating(seller.getId());
        Integer totalReviews = countSellerReviews(seller.getId());
        Map<String, Integer> ratingDistribution = calculateRatingDistribution(seller.getId());

        List<SellerProfileReviewResponse> reviewResponses = reviews.stream()
            .map(this::mapToReviewResponse)
            .collect(Collectors.toList());

        SellerProfileReviewsWrapper.ReviewStats stats = SellerProfileReviewsWrapper.ReviewStats.builder()
            .averageRating(averageRating != null ? averageRating : 0.0)
            .totalReviews(totalReviews)
            .ratingDistribution(ratingDistribution)
            .build();

        return SellerProfileReviewsWrapper.builder()
            .reviews(reviewResponses)
            .stats(stats)
            .build();
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private SellerProfileListingResponse mapToListingResponse(Listing listing) {
        String imageUrl = listing.getPhotos() != null && !listing.getPhotos().isEmpty()
                ? listing.getPhotos().get(0).getUrl()
                : null;
        
        String categoryName = listing.getCategory() != null 
                ? listing.getCategory().getName() 
                : "Uncategorized";

        // Get REAL rating from actual reviews for this listing (not fabricated book_meta rating!)
        Double rating = reviewRepository.findAverageRatingByListingId(listing.getId());

        return SellerProfileListingResponse.builder()
                .id(listing.getId())
                .title(listing.getBookMeta() != null ? listing.getBookMeta().getTitle() : "Unknown")
                .price(listing.getPrice())
                .image(imageUrl)
                .condition(listing.getCondition() != null ? listing.getCondition().name() : "GOOD")
                .rating(rating)
                .soldCount(listing.getSoldCount() != null ? listing.getSoldCount() : 0)
                .category(categoryName)
                .build();
    }

    private SellerProfileReviewResponse mapToReviewResponse(Review review) {
        String reviewerName = "Anonymous";
        String reviewerInitials = "A";
        
        if (review.getUser() != null) {
            UserProfile profile = review.getUser().getUserProfile();
            if (profile != null && profile.getFullName() != null) {
                reviewerName = profile.getFullName();
                reviewerInitials = generateInitials(profile.getFullName());
            } else if (review.getUser().getUsername() != null) {
                reviewerName = review.getUser().getUsername();
                reviewerInitials = review.getUser().getUsername().substring(0, 1).toUpperCase();
            }
        }

        return SellerProfileReviewResponse.builder()
                .id(review.getId())
                .reviewerName(reviewerName)
                .reviewerInitials(reviewerInitials)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .verifiedPurchase(review.getVerifiedPurchase() != null 
                        ? review.getVerifiedPurchase() : false)
                .build();
    }

    private Double calculateSellerAverageRating(Long sellerId) {
        try {
            return reviewRepository.calculateAverageRatingForSeller(sellerId);
        } catch (Exception e) {
            log.warn("Could not calculate average rating for seller {}: {}", sellerId, e.getMessage());
            return 0.0;
        }
    }

    private Integer countSellerReviews(Long sellerId) {
        try {
            return reviewRepository.countByListingSellerId(sellerId);
        } catch (Exception e) {
            log.warn("Could not count reviews for seller {}: {}", sellerId, e.getMessage());
            return 0;
        }
    }

    private Map<String, Integer> calculateRatingDistribution(Long sellerId) {
        Map<String, Integer> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(String.valueOf(i), 0);
        }
        
        try {
            List<Object[]> results = reviewRepository.getRatingDistributionForSeller(sellerId);
            for (Object[] row : results) {
                String rating = String.valueOf(((Number) row[0]).intValue());
                Integer count = ((Number) row[1]).intValue();
                distribution.put(rating, count);
            }
        } catch (Exception e) {
            log.warn("Could not calculate rating distribution for seller {}: {}", sellerId, e.getMessage());
        }
        
        return distribution;
    }

    private String formatMembershipDuration(LocalDate memberSince) {
        Period period = Period.between(memberSince, LocalDate.now());
        
        if (period.getYears() >= 1) {
            return period.getYears() + (period.getYears() == 1 ? " year" : " years");
        } else if (period.getMonths() >= 1) {
            return period.getMonths() + (period.getMonths() == 1 ? " month" : " months");
        } else {
            return "< 1 month";
        }
    }

    private String determineBadge(int totalSales, Double avgRating) {
        if (totalSales >= 1000 && avgRating != null && avgRating >= 4.8) {
            return "POWER_SELLER";
        } else if (totalSales >= 500 && avgRating != null && avgRating >= 4.5) {
            return "TOP_RATED";
        } else if (totalSales >= 100) {
            return "TRUSTED";
        } else if (totalSales >= 10) {
            return "ESTABLISHED";
        }
        return null;
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

    private Sort determineSortOrder(String sortBy) {
        return switch (sortBy) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "popular" -> Sort.by(Sort.Direction.DESC, "soldCount");
            case "rating" -> Sort.by(Sort.Direction.DESC, "bookMeta.averageRating");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
