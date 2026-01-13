package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.PublicStatsResponse;
import com.example.bookverseserver.enums.ListingStatus;
import com.example.bookverseserver.enums.OrderStatus;
import com.example.bookverseserver.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Service for public-facing platform data.
 * All methods return non-sensitive information only.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PublicService {
    
    UserRepository userRepository;
    UserProfileRepository userProfileRepository;
    ListingRepository listingRepository;
    OrderRepository orderRepository;
    CategoryRepository categoryRepository;
    ReviewRepository reviewRepository;
    
    /**
     * Get public platform statistics for homepage display.
     * Cached for 5 minutes to reduce database load.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "publicStats", key = "'stats'")
    public PublicStatsResponse getPublicStats() {
        log.debug("Fetching public platform stats");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime today = now.toLocalDate().atStartOfDay();
        
        // Total counts
        Long totalUsers = userRepository.count();
        Long casualSellers = userProfileRepository.countByAccountType("CASUAL_SELLER");
        Long proSellers = userProfileRepository.countByAccountType("PRO_SELLER");
        Long totalSellers = casualSellers + proSellers;
        Long totalBooks = listingRepository.countByStatus(ListingStatus.ACTIVE);
        Long totalOrders = orderRepository.countByStatusIn(
            Arrays.asList(OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERED)
        );
        Long categoryCount = categoryRepository.count();
        
        // Average rating across all reviews
        Double avgRating = reviewRepository.getAverageRating();
        if (avgRating == null) avgRating = 4.5; // Default if no reviews
        
        // Activity today
        Long ordersToday = orderRepository.countByCreatedAtAfter(today);
        Long newListingsToday = listingRepository.countByCreatedAtAfterAndStatus(today, ListingStatus.ACTIVE);
        
        return PublicStatsResponse.builder()
                .totalBooks(totalBooks)
                .totalSellers(totalSellers)
                .totalOrders(totalOrders)
                .totalUsers(totalUsers)
                .categoryCount(categoryCount)
                .avgRating(Math.round(avgRating * 10.0) / 10.0) // Round to 1 decimal
                .ordersToday(ordersToday)
                .newListingsToday(newListingsToday)
                .build();
    }
}
