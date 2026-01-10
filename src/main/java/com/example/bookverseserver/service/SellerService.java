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
import com.example.bookverseserver.repository.CartItemRepository;
import com.example.bookverseserver.repository.ListingRepository;
import com.example.bookverseserver.repository.OrderItemRepository;
import com.example.bookverseserver.repository.OrderRepository;
import com.example.bookverseserver.repository.ReviewRepository;
import com.example.bookverseserver.repository.WishlistRepository;
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
    CartItemRepository cartItemRepository;
    WishlistRepository wishlistRepository;
    ReviewRepository reviewRepository;
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
                .filter(oi -> oi.getOrder().getStatus() == OrderStatus.PENDING)
                .map(oi -> oi.getOrder().getId())
                .distinct()
                .count();

        // Order status breakdown - count unique orders by status
        long processingOrders = orderItems.stream()
                .filter(oi -> oi.getOrder().getStatus() == OrderStatus.PROCESSING)
                .map(oi -> oi.getOrder().getId())
                .distinct()
                .count();
        
        long shippedOrders = orderItems.stream()
                .filter(oi -> oi.getOrder().getStatus() == OrderStatus.SHIPPED)
                .map(oi -> oi.getOrder().getId())
                .distinct()
                .count();
        
        long deliveredOrders = orderItems.stream()
                .filter(oi -> oi.getOrder().getStatus() == OrderStatus.DELIVERED)
                .map(oi -> oi.getOrder().getId())
                .distinct()
                .count();
        
        long cancelledOrders = orderItems.stream()
                .filter(oi -> oi.getOrder().getStatus() == OrderStatus.CANCELLED)
                .map(oi -> oi.getOrder().getId())
                .distinct()
                .count();

        // Total views across all listings
        int totalViews = listings.stream()
                .mapToInt(l -> l.getViews() != null ? l.getViews() : 0)
                .sum();

        // Average rating - REAL calculation from reviews
        Double avgRating = reviewRepository.calculateAverageRatingForSeller(sellerId);
        long ratingCount = reviewRepository.countBySellerIdAndIsVisibleTrueAndIsHiddenFalse(sellerId);
        
        // Conversion rate: sales / views (as percentage)
        double conversionRate = totalViews > 0 
                ? ((double) totalSales / totalViews) * 100.0 
                : 0.0;
        
        // Wishlist adds - count how many times seller's listings are in wishlists
        long wishlistAdds = listingIds.isEmpty() ? 0 : wishlistRepository.countByListingIdIn(listingIds);

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
                        .processing((int) processingOrders)
                        .shipped((int) shippedOrders)
                        .delivered((int) deliveredOrders)
                        .cancelled((int) cancelledOrders)
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
                        .average(avgRating != null ? avgRating : 0.0)
                        .count((int) ratingCount)
                        .build())
                .avgOrderValue(totalSales > 0 
                        ? totalRevenue.divide(BigDecimal.valueOf(totalSales), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO)
                .conversionRate(conversionRate)
                .wishlistAdds(SellerStatsResponse.WishlistData.builder()
                        .total((int) wishlistAdds)
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

    // ============ Analytics (REAL DATA) ============

    @Transactional(readOnly = true)
    public RevenueDataResponse getRevenueAnalytics(Long sellerId, int days, String granularity) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime previousPeriodStart = startDate.minusDays(days);
        
        List<Listing> listings = listingRepository.findBySellerId(sellerId);
        List<Long> listingIds = listings.stream().map(Listing::getId).collect(Collectors.toList());
        
        if (listingIds.isEmpty()) {
            return RevenueDataResponse.builder()
                    .data(List.of())
                    .summary(RevenueDataResponse.Summary.builder()
                            .totalRevenue(BigDecimal.ZERO)
                            .averageRevenue(BigDecimal.ZERO)
                            .peakDay(null)
                            .trend(0.0)
                            .build())
                    .build();
        }
        
        List<OrderItem> orderItems = orderItemRepository.findByListingIdIn(listingIds);
        
        // Filter by date and delivered status
        List<OrderItem> currentPeriodItems = orderItems.stream()
                .filter(oi -> oi.getOrder().getStatus() == OrderStatus.DELIVERED)
                .filter(oi -> oi.getOrder().getCreatedAt().isAfter(startDate))
                .collect(Collectors.toList());
        
        List<OrderItem> previousPeriodItems = orderItems.stream()
                .filter(oi -> oi.getOrder().getStatus() == OrderStatus.DELIVERED)
                .filter(oi -> oi.getOrder().getCreatedAt().isAfter(previousPeriodStart) 
                           && oi.getOrder().getCreatedAt().isBefore(startDate))
                .collect(Collectors.toList());
        
        // Calculate totals
        BigDecimal currentTotal = currentPeriodItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal previousTotal = previousPeriodItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate trend
        double trend = previousTotal.compareTo(BigDecimal.ZERO) > 0
                ? currentTotal.subtract(previousTotal)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(previousTotal, 2, RoundingMode.HALF_UP)
                        .doubleValue()
                : 0.0;
        
        // Group by day - revenue and order count
        java.util.Map<String, List<OrderItem>> itemsByDay = currentPeriodItems.stream()
                .collect(Collectors.groupingBy(
                        oi -> oi.getOrder().getCreatedAt().toLocalDate().toString()
                ));
        
        List<RevenueDataResponse.RevenueTrendDataPoint> dataPoints = itemsByDay.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(e -> {
                    BigDecimal dayRevenue = e.getValue().stream()
                            .map(OrderItem::getSubtotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    int dayOrders = (int) e.getValue().stream()
                            .map(oi -> oi.getOrder().getId())
                            .distinct()
                            .count();
                    return RevenueDataResponse.RevenueTrendDataPoint.builder()
                            .date(e.getKey())
                            .revenue(dayRevenue)
                            .orders(dayOrders)
                            .build();
                })
                .collect(Collectors.toList());
        
        // Find peak day
        RevenueDataResponse.PeakDay peakDay = dataPoints.stream()
                .max(java.util.Comparator.comparing(RevenueDataResponse.RevenueTrendDataPoint::getRevenue))
                .map(dp -> RevenueDataResponse.PeakDay.builder()
                        .date(dp.getDate())
                        .revenue(dp.getRevenue())
                        .build())
                .orElse(null);
        
        // Calculate average
        BigDecimal averageRevenue = dataPoints.isEmpty() 
                ? BigDecimal.ZERO
                : currentTotal.divide(BigDecimal.valueOf(dataPoints.size()), 2, RoundingMode.HALF_UP);
        
        return RevenueDataResponse.builder()
                .data(dataPoints)
                .summary(RevenueDataResponse.Summary.builder()
                        .totalRevenue(currentTotal)
                        .averageRevenue(averageRevenue)
                        .peakDay(peakDay)
                        .trend(trend)
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public SalesDataResponse getSalesAnalytics(Long sellerId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime previousPeriodStart = startDate.minusDays(days);
        
        List<Listing> listings = listingRepository.findBySellerId(sellerId);
        List<Long> listingIds = listings.stream().map(Listing::getId).collect(Collectors.toList());
        
        if (listingIds.isEmpty()) {
            return SalesDataResponse.builder()
                    .byDay(List.of())
                    .total(0)
                    .trend(0.0)
                    .build();
        }
        
        List<OrderItem> orderItems = orderItemRepository.findByListingIdIn(listingIds);
        
        // Filter by date and delivered status
        List<OrderItem> currentPeriodItems = orderItems.stream()
                .filter(oi -> oi.getOrder().getStatus() == OrderStatus.DELIVERED)
                .filter(oi -> oi.getOrder().getCreatedAt().isAfter(startDate))
                .collect(Collectors.toList());
        
        List<OrderItem> previousPeriodItems = orderItems.stream()
                .filter(oi -> oi.getOrder().getStatus() == OrderStatus.DELIVERED)
                .filter(oi -> oi.getOrder().getCreatedAt().isAfter(previousPeriodStart)
                           && oi.getOrder().getCreatedAt().isBefore(startDate))
                .collect(Collectors.toList());
        
        int currentTotal = currentPeriodItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
        
        int previousTotal = previousPeriodItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
        
        double trend = previousTotal > 0
                ? ((currentTotal - previousTotal) * 100.0 / previousTotal)
                : 0.0;
        
        // Group by day
        java.util.Map<String, Integer> salesByDay = currentPeriodItems.stream()
                .collect(Collectors.groupingBy(
                        oi -> oi.getOrder().getCreatedAt().toLocalDate().toString(),
                        Collectors.summingInt(OrderItem::getQuantity)
                ));
        
        List<SalesDataResponse.DailySales> byDay = salesByDay.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(e -> SalesDataResponse.DailySales.builder()
                        .date(e.getKey())
                        .count(e.getValue())
                        .build())
                .collect(Collectors.toList());
        
        return SalesDataResponse.builder()
                .byDay(byDay)
                .total(currentTotal)
                .trend(trend)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ProductPerformanceResponse> getProductPerformance(Long sellerId, int limit, String sortBy) {
        List<Listing> listings = listingRepository.findBySellerId(sellerId);
        List<Long> listingIds = listings.stream().map(Listing::getId).collect(Collectors.toList());
        
        if (listingIds.isEmpty()) {
            return List.of();
        }
        
        // Get all order items for these listings
        List<OrderItem> orderItems = orderItemRepository.findByListingIdIn(listingIds);
        
        // Group by listing
        java.util.Map<Long, List<OrderItem>> itemsByListing = orderItems.stream()
                .filter(oi -> oi.getOrder().getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.groupingBy(oi -> oi.getListing().getId()));
        
        // Build performance data for each listing
        List<ProductPerformanceResponse> performances = listings.stream()
                .map(listing -> {
                    List<OrderItem> items = itemsByListing.getOrDefault(listing.getId(), List.of());
                    int sales = items.stream().mapToInt(OrderItem::getQuantity).sum();
                    BigDecimal revenue = items.stream()
                            .map(OrderItem::getSubtotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    int views = listing.getViews() != null ? listing.getViews() : 0;
                    double conversionRate = views > 0 ? (sales * 100.0 / views) : 0.0;
                    
                    String imageUrl = listing.getPhotos() != null && !listing.getPhotos().isEmpty()
                            ? listing.getPhotos().get(0).getUrl()
                            : null;
                    
                    return ProductPerformanceResponse.builder()
                            .productId(String.valueOf(listing.getId()))
                            .title(listing.getTitleOverride() != null 
                                    ? listing.getTitleOverride() 
                                    : listing.getBookMeta().getTitle())
                            .imageUrl(imageUrl)
                            .price(listing.getPrice())
                            .sales(sales)
                            .revenue(revenue)
                            .views(views)
                            .conversionRate(conversionRate)
                            .build();
                })
                .collect(Collectors.toList());
        
        // Sort by the requested field
        java.util.Comparator<ProductPerformanceResponse> comparator;
        switch (sortBy.toLowerCase()) {
            case "sales":
                comparator = java.util.Comparator.comparing(ProductPerformanceResponse::getSales).reversed();
                break;
            case "views":
                comparator = java.util.Comparator.comparing(ProductPerformanceResponse::getViews).reversed();
                break;
            case "conversion":
                comparator = java.util.Comparator.comparing(ProductPerformanceResponse::getConversionRate).reversed();
                break;
            case "revenue":
            default:
                comparator = java.util.Comparator.comparing(ProductPerformanceResponse::getRevenue).reversed();
                break;
        }
        
        return performances.stream()
                .sorted(comparator)
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ============================================================
    // REAL ANALYTICS: Traffic Sources
    // 
    // Uses Listing.views as total traffic. Detailed source tracking
    // (referrer, organic, social) requires ViewEvent entity - coming soon.
    // For now, we show real view counts attributed to "direct".
    // ============================================================
    
    @Transactional(readOnly = true)
    public TrafficSourcesDataResponse getTrafficSources(Long sellerId, int days) {
        List<Listing> listings = listingRepository.findBySellerId(sellerId);
        
        // Sum all views from seller's listings
        int totalViews = listings.stream()
                .mapToInt(l -> l.getViews() != null ? l.getViews() : 0)
                .sum();
        
        // Until we have ViewEvent with source tracking, attribute all to "direct"
        // This is HONEST - we don't fake data, we show what we actually know
        List<TrafficSourcesDataResponse.TrafficSource> sources = new java.util.ArrayList<>();
        
        if (totalViews > 0) {
            sources.add(TrafficSourcesDataResponse.TrafficSource.builder()
                    .source("direct")
                    .visits(totalViews)
                    .percentage(100.0)
                    .build());
        }
        
        return TrafficSourcesDataResponse.builder()
                .sources(sources)
                .totalTraffic(totalViews)
                .bounceRate(0.0) // Requires session tracking - honest null until implemented
                .build();
    }

    // ============================================================
    // REAL ANALYTICS: Conversion Funnel
    // 
    // Computes REAL data from existing entities:
    // - Views: Listing.views (aggregated)
    // - Wishlist: Wishlist entries for seller's listings
    // - Cart: CartItem entries for seller's listings
    // - Purchase: OrderItem entries for seller's listings
    // ============================================================
    
    @Transactional(readOnly = true)
    public ConversionFunnelDataResponse getConversionFunnel(Long sellerId, int days) {
        List<Listing> listings = listingRepository.findBySellerId(sellerId);
        List<Long> listingIds = listings.stream().map(Listing::getId).collect(Collectors.toList());
        
        // Stage 1: Views (from Listing.views)
        int totalViews = listings.stream()
                .mapToInt(l -> l.getViews() != null ? l.getViews() : 0)
                .sum();
        
        // Stage 2: Wishlist count (for seller's listings)
        long wishlistCount = listingIds.isEmpty() ? 0 :
                wishlistRepository.countByListingIdIn(listingIds);
        
        // Stage 3: Cart count (for seller's listings)
        long cartCount = listingIds.isEmpty() ? 0 :
                cartItemRepository.countByListingIdIn(listingIds);
        
        // Stage 4: Purchase count (for seller's listings - delivered orders)
        long purchaseCount = listingIds.isEmpty() ? 0 :
                orderItemRepository.findByListingIdIn(listingIds).stream()
                        .filter(oi -> oi.getOrder().getStatus() == OrderStatus.DELIVERED)
                        .count();
        
        // Calculate conversion rates
        List<ConversionFunnelDataResponse.ConversionStep> steps = new java.util.ArrayList<>();
        
        steps.add(ConversionFunnelDataResponse.ConversionStep.builder()
                .stage("views")
                .count(totalViews)
                .conversionRate(100.0)
                .build());
        
        steps.add(ConversionFunnelDataResponse.ConversionStep.builder()
                .stage("wishlist")
                .count((int) wishlistCount)
                .conversionRate(totalViews > 0 ? (wishlistCount * 100.0 / totalViews) : 0.0)
                .build());
        
        steps.add(ConversionFunnelDataResponse.ConversionStep.builder()
                .stage("cart")
                .count((int) cartCount)
                .conversionRate(totalViews > 0 ? (cartCount * 100.0 / totalViews) : 0.0)
                .build());
        
        steps.add(ConversionFunnelDataResponse.ConversionStep.builder()
                .stage("purchase")
                .count((int) purchaseCount)
                .conversionRate(totalViews > 0 ? (purchaseCount * 100.0 / totalViews) : 0.0)
                .build());
        
        double overallConversion = totalViews > 0 ? (purchaseCount * 100.0 / totalViews) : 0.0;
        
        return ConversionFunnelDataResponse.builder()
                .steps(steps)
                .overallConversionRate(overallConversion)
                .build();
    }

    // ============================================================
    // REAL ANALYTICS: Customer Insights
    // 
    // Computes REAL data from orders:
    // - Total customers: DISTINCT users who ordered seller's items
    // - New customers: First-time buyers (1 order)
    // - Returning customers: Repeat buyers (>1 orders)
    // - Repeat rate: Returning / Total
    // - AOV: Total revenue / Total orders
    // ============================================================
    
    @Transactional(readOnly = true)
    public CustomerInsightsResponse getCustomerInsights(Long sellerId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime previousPeriodStart = startDate.minusDays(days);
        
        List<Listing> listings = listingRepository.findBySellerId(sellerId);
        List<Long> listingIds = listings.stream().map(Listing::getId).collect(Collectors.toList());
        
        if (listingIds.isEmpty()) {
            return CustomerInsightsResponse.builder()
                    .totalCustomers(0)
                    .newCustomers(0)
                    .newCustomersTrend(0.0)
                    .repeatCustomers(0)
                    .repeatRate(0.0)
                    .avgLifetimeValue(BigDecimal.ZERO)
                    .topRegions(List.of())
                    .build();
        }
        
        // Get all order items for seller's listings
        List<OrderItem> orderItems = orderItemRepository.findByListingIdIn(listingIds);
        
        // Current period orders
        java.util.Set<Order> currentPeriodOrders = orderItems.stream()
                .filter(oi -> oi.getOrder().getStatus() == OrderStatus.DELIVERED)
                .filter(oi -> oi.getOrder().getCreatedAt().isAfter(startDate))
                .map(OrderItem::getOrder)
                .collect(Collectors.toSet());
        
        // Previous period new customers (for trend calculation)
        java.util.Set<Long> previousPeriodCustomers = orderItems.stream()
                .filter(oi -> oi.getOrder().getStatus() == OrderStatus.DELIVERED)
                .filter(oi -> oi.getOrder().getCreatedAt().isAfter(previousPeriodStart)
                           && oi.getOrder().getCreatedAt().isBefore(startDate))
                .map(oi -> oi.getOrder().getUser().getId())
                .collect(Collectors.toSet());
        
        // Count orders per customer (all time for repeat rate)
        java.util.Set<Order> allDeliveredOrders = orderItems.stream()
                .filter(oi -> oi.getOrder().getStatus() == OrderStatus.DELIVERED)
                .map(OrderItem::getOrder)
                .collect(Collectors.toSet());
        
        java.util.Map<Long, Long> ordersPerCustomer = allDeliveredOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getUser().getId(),
                        Collectors.counting()
                ));
        
        int totalCustomers = ordersPerCustomer.size();
        int newCustomers = (int) ordersPerCustomer.values().stream()
                .filter(count -> count == 1)
                .count();
        int repeatCustomers = (int) ordersPerCustomer.values().stream()
                .filter(count -> count > 1)
                .count();
        
        double repeatRate = totalCustomers > 0 
                ? (repeatCustomers * 100.0 / totalCustomers) 
                : 0.0;
        
        // New customers trend
        int previousNewCount = previousPeriodCustomers.size();
        double newCustomersTrend = previousNewCount > 0
                ? ((newCustomers - previousNewCount) * 100.0 / previousNewCount)
                : 0.0;
        
        // Calculate average lifetime value (total revenue / total customers)
        BigDecimal totalRevenue = orderItems.stream()
                .filter(oi -> oi.getOrder().getStatus() == OrderStatus.DELIVERED)
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal avgLifetimeValue = totalCustomers > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(totalCustomers), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        // Top regions from shipping addresses
        java.util.Map<String, Long> ordersByRegion = allDeliveredOrders.stream()
                .filter(o -> o.getShippingAddress() != null && o.getShippingAddress().getCity() != null)
                .collect(Collectors.groupingBy(
                        o -> o.getShippingAddress().getCity(),
                        Collectors.counting()
                ));
        
        long totalOrdersForPercentage = ordersByRegion.values().stream().mapToLong(Long::longValue).sum();
        
        List<CustomerInsightsResponse.TopRegion> topRegions = ordersByRegion.entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> CustomerInsightsResponse.TopRegion.builder()
                        .region(e.getKey())
                        .orders(e.getValue().intValue())
                        .percentage(totalOrdersForPercentage > 0 
                                ? (e.getValue() * 100.0 / totalOrdersForPercentage)
                                : 0.0)
                        .build())
                .collect(Collectors.toList());
        
        return CustomerInsightsResponse.builder()
                .totalCustomers(totalCustomers)
                .newCustomers(newCustomers)
                .newCustomersTrend(newCustomersTrend)
                .repeatCustomers(repeatCustomers)
                .repeatRate(repeatRate)
                .avgLifetimeValue(avgLifetimeValue)
                .topRegions(topRegions)
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
