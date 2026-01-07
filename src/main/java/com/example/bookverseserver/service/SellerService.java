package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.Analytics.*;
import com.example.bookverseserver.dto.response.Order.OrderDTO;
import com.example.bookverseserver.dto.response.Order.OrderListResponse;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.dto.response.Product.ListingResponse;
import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.Order_Payment.OrderItem;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.enums.ListingStatus;
import com.example.bookverseserver.enums.OrderStatus;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.ListingMapper;
import com.example.bookverseserver.mapper.OrderMapper;
import com.example.bookverseserver.repository.ListingRepository;
import com.example.bookverseserver.repository.OrderItemRepository;
import com.example.bookverseserver.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Seller Dashboard Service - per Vision API_CONTRACTS.md §7 Seller Operations.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SellerService {

    ListingRepository listingRepository;
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    ListingMapper listingMapper;
    OrderMapper orderMapper;

    // ============ Dashboard Stats ============

    @Transactional(readOnly = true)
    public SellerStatsResponse getSellerStats(Long sellerId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        // Get seller's listings
        List<Listing> listings = listingRepository.findBySellerId(sellerId);
        
        // Get order items for seller's listings
        List<Long> listingIds = listings.stream().map(Listing::getId).collect(Collectors.toList());
        List<OrderItem> orderItems = orderItemRepository.findByListingIdIn(listingIds);
        
        // Calculate stats
        int totalListings = listings.size();
        int activeListings = (int) listings.stream()
                .filter(l -> l.getStatus() == ListingStatus.ACTIVE)
                .count();
        int draftListings = (int) listings.stream()
                .filter(l -> l.getStatus() == ListingStatus.DRAFT)
                .count();
        int soldOutListings = (int) listings.stream()
                .filter(l -> l.getStatus() == ListingStatus.SOLD_OUT)
                .count();

        // Revenue calculation
        BigDecimal totalRevenue = orderItems.stream()
                .filter(oi -> oi.getOrder().getStatus() == OrderStatus.DELIVERED)
                .map(oi -> oi.getSubtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total sales count
        int totalSales = orderItems.stream()
                .filter(oi -> oi.getOrder().getStatus() == OrderStatus.DELIVERED)
                .mapToInt(OrderItem::getQuantity)
                .sum();

        // Pending orders (not delivered yet)
        long pendingOrders = orderItems.stream()
                .filter(oi -> oi.getOrder().getStatus() == OrderStatus.PENDING 
                        || oi.getOrder().getStatus() == OrderStatus.PROCESSING)
                .map(oi -> oi.getOrder().getId())
                .distinct()
                .count();

        // Total views across all listings
        int totalViews = listings.stream()
                .mapToInt(l -> l.getViews() != null ? l.getViews() : 0)
                .sum();

        // Average rating (from seller profile, or calculate from reviews)
        Double avgRating = 4.5; // TODO: Calculate from actual reviews
        Integer ratingCount = 0;

        // Build response
        return SellerStatsResponse.builder()
                .revenue(RevenueData.builder()
                        .total(totalRevenue)
                        .trend(0.0) // TODO: Calculate trend
                        .build())
                .sales(SalesData.builder()
                        .total(totalSales)
                        .trend(0.0)
                        .build())
                .orders(OrdersBreakdown.builder()
                        .pending((int) pendingOrders)
                        .processing(0)
                        .shipped(0)
                        .delivered(0)
                        .cancelled(0)
                        .build())
                .listings(ListingsStats.builder()
                        .total(totalListings)
                        .active(activeListings)
                        .pending(draftListings)
                        .outOfStock(soldOutListings)
                        .build())
                .views(ViewsData.builder()
                        .total(totalViews)
                        .trend(0.0)
                        .build())
                .rating(RatingData.builder()
                        .average(avgRating)
                        .count(ratingCount)
                        .build())
                .avgOrderValue(totalSales > 0 
                        ? totalRevenue.divide(BigDecimal.valueOf(totalSales), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO)
                .conversionRate(0.0) // TODO: Calculate from views/sales
                .wishlistAdds(SellerStatsResponse.WishlistData.builder()
                        .total(0)
                        .trend(0.0)
                        .build())
                .build();
    }

    // ============ Seller Listings ============

    @Transactional(readOnly = true)
    public PagedResponse<ListingResponse> getSellerListings(
            Long sellerId, 
            ListingStatus status,
            String sortBy, 
            String sortOrder,
            int page, 
            int limit) {
        
        Sort sort = sortOrder.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        // Convert 1-indexed page to 0-indexed for Spring
        PageRequest pageRequest = PageRequest.of(page - 1, limit, sort);
        
        Page<Listing> listingsPage;
        if (status != null) {
            listingsPage = listingRepository.findBySellerIdAndStatus(sellerId, status, pageRequest);
        } else {
            listingsPage = listingRepository.findBySellerIdAndStatusNot(
                    sellerId, ListingStatus.REMOVED, pageRequest);
        }

        List<ListingResponse> responses = listingsPage.getContent().stream()
                .map(listingMapper::toListingResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                responses,
                listingsPage.getNumber(),
                listingsPage.getSize(),
                listingsPage.getTotalElements(),
                listingsPage.getTotalPages()
        );
    }

    // ============ Seller Orders ============

    @Transactional(readOnly = true)
    public OrderListResponse getSellerOrders(
            Long sellerId,
            OrderStatus status,
            String sortBy,
            String sortOrder,
            int page,
            int limit) {
        
        // Get orders that contain seller's listings
        List<Listing> sellerListings = listingRepository.findBySellerId(sellerId);
        List<Long> listingIds = sellerListings.stream()
                .map(Listing::getId)
                .collect(Collectors.toList());

        // Find distinct orders containing these listings
        List<OrderItem> orderItems = orderItemRepository.findByListingIdIn(listingIds);
        
        List<Order> allOrders = orderItems.stream()
                .map(OrderItem::getOrder)
                .distinct()
                .filter(o -> status == null || o.getStatus() == status)
                .sorted((a, b) -> sortOrder.equalsIgnoreCase("asc") 
                        ? a.getCreatedAt().compareTo(b.getCreatedAt())
                        : b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
        
        long totalItems = allOrders.size();
        int totalPages = (int) Math.ceil((double) totalItems / limit);
        
        List<Order> pagedOrders = allOrders.stream()
                .skip((long) (page - 1) * limit)
                .limit(limit)
                .collect(Collectors.toList());

        // Map orders to DTOs
        List<OrderDTO> orderDTOs = orderMapper.toOrderDTOList(pagedOrders);
        
        return OrderListResponse.builder()
                .orders(orderDTOs)
                .pagination(OrderListResponse.PaginationInfo.builder()
                        .page(page)
                        .limit(limit)
                        .totalItems(totalItems)
                        .totalPages(totalPages)
                        .hasNext(page < totalPages)
                        .hasPrevious(page > 1)
                        .build())
                .build();
    }

    // ============ Analytics ============

    @Transactional(readOnly = true)
    public RevenueDataResponse getRevenueAnalytics(Long sellerId, int days, String granularity) {
        // TODO: Implement time-series revenue data
        return RevenueDataResponse.builder()
                .byDay(List.of())
                .total(BigDecimal.ZERO)
                .trend(0.0)
                .build();
    }

    @Transactional(readOnly = true)
    public SalesDataResponse getSalesAnalytics(Long sellerId, int days) {
        // TODO: Implement time-series sales data
        return SalesDataResponse.builder()
                .byDay(List.of())
                .total(0)
                .trend(0.0)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ProductPerformanceResponse> getProductPerformance(Long sellerId, int limit, String sortBy) {
        // TODO: Implement product performance ranking
        return List.of();
    }

    // ============ Advanced Analytics (Stubs) ============
    
    /**
     * STUB: Traffic sources analytics.
     * TODO: Implement real tracking in future sprint.
     */
    @Transactional(readOnly = true)
    public com.example.bookverseserver.dto.response.Seller.TrafficSourcesResponse getTrafficSources(Long sellerId, int days) {
        log.debug("getTrafficSources called for seller {} - returning stub data", sellerId);
        return com.example.bookverseserver.dto.response.Seller.TrafficSourcesResponse.builder()
                .sources(List.of(
                        com.example.bookverseserver.dto.response.Seller.TrafficSourcesResponse.TrafficSource.builder()
                                .source("Direct")
                                .visitors(0L)
                                .percentage(0.0)
                                .build(),
                        com.example.bookverseserver.dto.response.Seller.TrafficSourcesResponse.TrafficSource.builder()
                                .source("Search")
                                .visitors(0L)
                                .percentage(0.0)
                                .build()
                ))
                .totalTraffic(0L)
                .bounceRate(0.0)
                .build();
    }

    /**
     * STUB: Conversion funnel analytics.
     * TODO: Implement real tracking in future sprint.
     */
    @Transactional(readOnly = true)
    public com.example.bookverseserver.dto.response.Seller.ConversionFunnelResponse getConversionFunnel(Long sellerId, int days) {
        log.debug("getConversionFunnel called for seller {} - returning stub data", sellerId);
        return com.example.bookverseserver.dto.response.Seller.ConversionFunnelResponse.builder()
                .steps(List.of(
                        com.example.bookverseserver.dto.response.Seller.ConversionFunnelResponse.FunnelStep.builder()
                                .stage("views")
                                .count(0L)
                                .conversionRate(0.0)
                                .build(),
                        com.example.bookverseserver.dto.response.Seller.ConversionFunnelResponse.FunnelStep.builder()
                                .stage("wishlist")
                                .count(0L)
                                .conversionRate(0.0)
                                .build(),
                        com.example.bookverseserver.dto.response.Seller.ConversionFunnelResponse.FunnelStep.builder()
                                .stage("cart")
                                .count(0L)
                                .conversionRate(0.0)
                                .build(),
                        com.example.bookverseserver.dto.response.Seller.ConversionFunnelResponse.FunnelStep.builder()
                                .stage("checkout")
                                .count(0L)
                                .conversionRate(0.0)
                                .build(),
                        com.example.bookverseserver.dto.response.Seller.ConversionFunnelResponse.FunnelStep.builder()
                                .stage("purchase")
                                .count(0L)
                                .conversionRate(0.0)
                                .build()
                ))
                .overallConversionRate(0.0)
                .build();
    }

    /**
     * STUB: Customer insights analytics.
     * TODO: Implement real tracking in future sprint.
     */
    @Transactional(readOnly = true)
    public com.example.bookverseserver.dto.response.Seller.CustomerInsightsResponse getCustomerInsights(Long sellerId, int days) {
        log.debug("getCustomerInsights called for seller {} - returning stub data", sellerId);
        return com.example.bookverseserver.dto.response.Seller.CustomerInsightsResponse.builder()
                .totalCustomers(0L)
                .repeatCustomers(0L)
                .repeatRate(0.0)
                .newCustomers(0L)
                .newCustomersTrend(0.0)
                .avgLifetimeValue(BigDecimal.ZERO)
                .topRegions(List.of())
                .build();
    }

    // ============ Quick Actions ============

    @Transactional
    public ListingResponse activateListing(Long sellerId, Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND));
        
        // Verify ownership
        if (!listing.getSeller().getId().equals(sellerId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        listing.setStatus(ListingStatus.ACTIVE);
        Listing saved = listingRepository.save(listing);
        return listingMapper.toListingResponse(saved);
    }

    @Transactional
    public ListingResponse deactivateListing(Long sellerId, Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND));
        
        // Verify ownership
        if (!listing.getSeller().getId().equals(sellerId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        listing.setStatus(ListingStatus.DRAFT);
        Listing saved = listingRepository.save(listing);
        return listingMapper.toListingResponse(saved);
    }
}
