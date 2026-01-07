package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Order.UpdateOrderStatusRequest;
import com.example.bookverseserver.dto.response.Analytics.*;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Order.OrderListResponse;
import com.example.bookverseserver.dto.response.Order.UpdateOrderStatusResponse;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.dto.response.Product.ListingResponse;
import com.example.bookverseserver.enums.ListingStatus;
import com.example.bookverseserver.enums.OrderStatus;
import com.example.bookverseserver.service.OrderService;
import com.example.bookverseserver.service.SellerService;
import com.example.bookverseserver.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Seller Dashboard API - per Vision API_CONTRACTS.md §7 Seller Operations.
 * 
 * Provides seller-specific views of their data:
 * - Dashboard stats (revenue, sales, orders, listings)
 * - Seller's listings with filters
 * - Seller's orders with filters
 * - Order status updates with tracking
 * - Analytics data
 */
@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Seller Dashboard", description = "APIs for seller dashboard and analytics")
public class SellerController {

    SellerService sellerService;
    OrderService orderService;
    SecurityUtils securityUtils;

    // ============ Dashboard Stats ============

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    @Operation(summary = "Get seller dashboard stats", 
               description = "Returns aggregated stats for seller dashboard: revenue, sales, orders, listings, views, rating")
    public ApiResponse<SellerStatsResponse> getDashboardStats(
            @RequestParam(defaultValue = "30") int days,
            Authentication authentication) {
        Long sellerId = securityUtils.getCurrentUserId(authentication);
        return ApiResponse.<SellerStatsResponse>builder()
                .message("Dashboard stats retrieved successfully")
                .result(sellerService.getSellerStats(sellerId, days))
                .build();
    }

    // ============ Seller Listings ============

    @GetMapping("/listings")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    @Operation(summary = "Get seller's listings", 
               description = "Returns paginated list of seller's own listings with optional filters")
    public ApiResponse<PagedResponse<ListingResponse>> getSellerListings(
            @RequestParam(required = false) ListingStatus status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            Authentication authentication) {
        Long sellerId = securityUtils.getCurrentUserId(authentication);
        return ApiResponse.<PagedResponse<ListingResponse>>builder()
                .message("Seller listings retrieved successfully")
                .result(sellerService.getSellerListings(sellerId, status, sortBy, sortOrder, page, limit))
                .build();
    }

    // ============ Seller Orders ============

    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    @Operation(summary = "Get seller's orders", 
               description = "Returns paginated list of orders containing seller's listings")
    public ApiResponse<OrderListResponse> getSellerOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            Authentication authentication) {
        Long sellerId = securityUtils.getCurrentUserId(authentication);
        return ApiResponse.<OrderListResponse>builder()
                .message("Seller orders retrieved successfully")
                .result(sellerService.getSellerOrders(sellerId, status, sortBy, sortOrder, page, limit))
                .build();
    }

    // ============ Analytics ============

    @GetMapping("/analytics/revenue")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    @Operation(summary = "Get revenue analytics", 
               description = "Returns revenue data over time for charts")
    public ApiResponse<RevenueDataResponse> getRevenueAnalytics(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "daily") String granularity,
            Authentication authentication) {
        Long sellerId = securityUtils.getCurrentUserId(authentication);
        return ApiResponse.<RevenueDataResponse>builder()
                .message("Revenue analytics retrieved successfully")
                .result(sellerService.getRevenueAnalytics(sellerId, days, granularity))
                .build();
    }

    @GetMapping("/analytics/sales")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    @Operation(summary = "Get sales analytics", 
               description = "Returns sales data over time for charts")
    public ApiResponse<SalesDataResponse> getSalesAnalytics(
            @RequestParam(defaultValue = "30") int days,
            Authentication authentication) {
        Long sellerId = securityUtils.getCurrentUserId(authentication);
        return ApiResponse.<SalesDataResponse>builder()
                .message("Sales analytics retrieved successfully")
                .result(sellerService.getSalesAnalytics(sellerId, days))
                .build();
    }

    @GetMapping("/analytics/products")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    @Operation(summary = "Get product performance", 
               description = "Returns top performing products by views, sales, revenue")
    public ApiResponse<List<ProductPerformanceResponse>> getProductPerformance(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "revenue") String sortBy,
            Authentication authentication) {
        Long sellerId = securityUtils.getCurrentUserId(authentication);
        return ApiResponse.<List<ProductPerformanceResponse>>builder()
                .message("Product performance retrieved successfully")
                .result(sellerService.getProductPerformance(sellerId, limit, sortBy))
                .build();
    }

    @GetMapping("/analytics/traffic")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    @Operation(summary = "Get traffic sources (STUB)", 
               description = "Returns traffic source breakdown. NOTE: Currently returns placeholder data.")
    public ApiResponse<com.example.bookverseserver.dto.response.Seller.TrafficSourcesResponse> getTrafficSources(
            @RequestParam(defaultValue = "30") int days,
            Authentication authentication) {
        Long sellerId = securityUtils.getCurrentUserId(authentication);
        return ApiResponse.<com.example.bookverseserver.dto.response.Seller.TrafficSourcesResponse>builder()
                .message("Traffic sources retrieved (placeholder data)")
                .result(sellerService.getTrafficSources(sellerId, days))
                .build();
    }

    @GetMapping("/analytics/conversion")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    @Operation(summary = "Get conversion funnel (STUB)", 
               description = "Returns conversion funnel stages. NOTE: Currently returns placeholder data.")
    public ApiResponse<com.example.bookverseserver.dto.response.Seller.ConversionFunnelResponse> getConversionFunnel(
            @RequestParam(defaultValue = "30") int days,
            Authentication authentication) {
        Long sellerId = securityUtils.getCurrentUserId(authentication);
        return ApiResponse.<com.example.bookverseserver.dto.response.Seller.ConversionFunnelResponse>builder()
                .message("Conversion funnel retrieved (placeholder data)")
                .result(sellerService.getConversionFunnel(sellerId, days))
                .build();
    }

    @GetMapping("/analytics/customers")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    @Operation(summary = "Get customer insights (STUB)", 
               description = "Returns customer analytics. NOTE: Currently returns placeholder data.")
    public ApiResponse<com.example.bookverseserver.dto.response.Seller.CustomerInsightsResponse> getCustomerInsights(
            @RequestParam(defaultValue = "30") int days,
            Authentication authentication) {
        Long sellerId = securityUtils.getCurrentUserId(authentication);
        return ApiResponse.<com.example.bookverseserver.dto.response.Seller.CustomerInsightsResponse>builder()
                .message("Customer insights retrieved (placeholder data)")
                .result(sellerService.getCustomerInsights(sellerId, days))
                .build();
    }

    // ============ Quick Actions ============

    @PostMapping("/listings/{listingId}/activate")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    @Operation(summary = "Activate a listing", 
               description = "Changes listing status from DRAFT to ACTIVE")
    public ApiResponse<ListingResponse> activateListing(
            @PathVariable Long listingId,
            Authentication authentication) {
        Long sellerId = securityUtils.getCurrentUserId(authentication);
        return ApiResponse.<ListingResponse>builder()
                .message("Listing activated successfully")
                .result(sellerService.activateListing(sellerId, listingId))
                .build();
    }

    @PostMapping("/listings/{listingId}/deactivate")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    @Operation(summary = "Deactivate a listing", 
               description = "Changes listing status to DRAFT (soft hide)")
    public ApiResponse<ListingResponse> deactivateListing(
            @PathVariable Long listingId,
            Authentication authentication) {
        Long sellerId = securityUtils.getCurrentUserId(authentication);
        return ApiResponse.<ListingResponse>builder()
                .message("Listing deactivated successfully")
                .result(sellerService.deactivateListing(sellerId, listingId))
                .build();
    }

    // ============ Order Management ============

    @PatchMapping("/orders/{orderId}/status")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    @Operation(summary = "Update order status", 
               description = "Seller updates order status with tracking info. Per Vision API_CONTRACTS.md")
    public ApiResponse<UpdateOrderStatusResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            Authentication authentication) {
        Long sellerId = securityUtils.getCurrentUserId(authentication);
        return ApiResponse.<UpdateOrderStatusResponse>builder()
                .message("Order status updated successfully")
                .result(orderService.updateOrderStatusBySeller(sellerId, orderId, request))
                .build();
    }
}
