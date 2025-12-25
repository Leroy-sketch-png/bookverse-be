package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Order.CancelOrderRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Order.CancelOrderResponse;
import com.example.bookverseserver.dto.response.Order.OrderDTO;
import com.example.bookverseserver.dto.response.Order.OrderListResponse;
import com.example.bookverseserver.dto.response.Order.OrderTrackingDTO;
import com.example.bookverseserver.enums.OrderStatus;
import com.example.bookverseserver.service.OrderService;
import com.example.bookverseserver.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final SecurityUtils securityUtils;

  @GetMapping
  public ApiResponse<OrderListResponse> getUserOrders(
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortOrder,
      Authentication authentication) {
    Long userId = securityUtils.getCurrentUserId(authentication);
    return ApiResponse.<OrderListResponse>builder()
        .message("Orders retrieved successfully")
        .result(orderService.getUserOrders(userId, status, page, limit, sortBy, sortOrder))
        .build();
  }

  @GetMapping("/{orderId}")
  public ApiResponse<OrderDTO> getOrderDetails(
      @PathVariable UUID orderId,
      Authentication authentication) {
    Long userId = securityUtils.getCurrentUserId(authentication);
    return ApiResponse.<OrderDTO>builder()
        .message("Order details retrieved successfully")
        .result(orderService.getOrderDetails(userId, orderId))
        .build();
  }

  @PostMapping("/{orderId}/cancel")
  public ApiResponse<CancelOrderResponse> cancelOrder(
      @PathVariable UUID orderId,
      @RequestBody CancelOrderRequest request,
      Authentication authentication) {
    Long userId = securityUtils.getCurrentUserId(authentication);
    return ApiResponse.<CancelOrderResponse>builder()
        .message("Order cancelled successfully")
        .result(orderService.cancelOrder(userId, orderId, request))
        .build();
  }

  @GetMapping("/{orderId}/tracking")
  public ApiResponse<OrderTrackingDTO> getOrderTracking(
      @PathVariable UUID orderId,
      Authentication authentication) {
    Long userId = securityUtils.getCurrentUserId(authentication);
    return ApiResponse.<OrderTrackingDTO>builder()
        .message("Order tracking retrieved successfully")
        .result(orderService.getOrderTracking(userId, orderId))
        .build();
  }
}
