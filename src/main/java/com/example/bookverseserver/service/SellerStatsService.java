package com.example.bookverseserver.service;

import com.example.bookverseserver.enums.OrderStatus;
import com.example.bookverseserver.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service for calculating seller performance statistics from actual order data.
 * These are CALCULATED values, not self-declared.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SellerStatsService {
    
    OrderRepository orderRepository;
    
    // Statuses that count as "fulfilled" (seller did their job)
    private static final List<OrderStatus> FULFILLED_STATUSES = List.of(
            OrderStatus.SHIPPED,
            OrderStatus.DELIVERED
    );
    
    // Statuses to exclude from total count (system-cancelled, not seller's fault)
    private static final List<OrderStatus> EXCLUDED_STATUSES = List.of(
            OrderStatus.CANCELLED
    );
    
    /**
     * Calculate seller's fulfillment rate as a percentage.
     * Formula: (shipped + delivered orders) / (total orders excluding cancelled) * 100
     * 
     * @param sellerId The seller's user ID
     * @return Fulfillment rate as percentage (0-100), or null if no orders
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateFulfillmentRate(Long sellerId) {
        Long totalOrders = orderRepository.countOrdersBySellerId(sellerId, EXCLUDED_STATUSES);
        if (totalOrders == null || totalOrders == 0) {
            return null; // No orders yet
        }
        
        Long fulfilledOrders = orderRepository.countFulfilledOrdersBySellerId(sellerId, FULFILLED_STATUSES);
        if (fulfilledOrders == null) {
            fulfilledOrders = 0L;
        }
        
        return BigDecimal.valueOf(fulfilledOrders)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalOrders), 1, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate seller's average response time (time to ship).
     * Based on actual order data: average time from order creation to shipping.
     * 
     * @param sellerId The seller's user ID
     * @return Human-readable response time string, or null if no shipped orders
     */
    @Transactional(readOnly = true)
    public String calculateResponseTime(Long sellerId) {
        Double avgHours = orderRepository.calculateAverageShippingTimeHours(sellerId);
        if (avgHours == null) {
            return null; // No shipped orders yet
        }
        
        return formatHoursToReadable(avgHours);
    }
    
    /**
     * Get complete seller stats as a record.
     */
    @Transactional(readOnly = true)
    public SellerStats getSellerStats(Long sellerId) {
        return new SellerStats(
                calculateFulfillmentRate(sellerId),
                calculateResponseTime(sellerId)
        );
    }
    
    /**
     * Format hours into a human-readable string.
     */
    private String formatHoursToReadable(double hours) {
        if (hours < 1) {
            int minutes = (int) Math.round(hours * 60);
            return "< " + Math.max(30, minutes) + " min";
        } else if (hours < 2) {
            return "< 2 hours";
        } else if (hours < 4) {
            return "2-4 hours";
        } else if (hours < 8) {
            return "4-8 hours";
        } else if (hours < 24) {
            return "< 1 day";
        } else if (hours < 48) {
            return "1-2 days";
        } else if (hours < 72) {
            return "2-3 days";
        } else {
            int days = (int) Math.round(hours / 24);
            return days + "+ days";
        }
    }
    
    /**
     * Record to hold calculated seller stats.
     */
    public record SellerStats(
            BigDecimal fulfillmentRate,
            String responseTime
    ) {
        /**
         * Whether stats are "verified" (calculated from data) or not.
         */
        public boolean isVerified() {
            return fulfillmentRate != null || responseTime != null;
        }
    }
}
