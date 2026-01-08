package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Order.CancelOrderRequest;
import com.example.bookverseserver.dto.request.Order.UpdateOrderStatusRequest;
import com.example.bookverseserver.dto.response.Order.CancelOrderResponse;
import com.example.bookverseserver.dto.response.Order.OrderDTO;
import com.example.bookverseserver.dto.response.Order.OrderListResponse;
import com.example.bookverseserver.dto.response.Order.OrderTrackingDTO;
import com.example.bookverseserver.dto.response.Order.UpdateOrderStatusResponse;
import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.Order_Payment.OrderTimeline;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.OrderStatus;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.OrderMapper;
import com.example.bookverseserver.repository.OrderItemRepository;
import com.example.bookverseserver.repository.OrderRepository;
import com.example.bookverseserver.repository.OrderTimelineRepository;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderService {

  OrderRepository orderRepository;
  OrderItemRepository orderItemRepository;
  OrderTimelineRepository orderTimelineRepository;
  UserRepository userRepository;
  OrderMapper orderMapper;
  TransactionService transactionService;
  
  // Valid status transitions for seller
  private static final Set<OrderStatus> SELLER_ALLOWED_STATUSES = Set.of(
      OrderStatus.PROCESSING, 
      OrderStatus.SHIPPED, 
      OrderStatus.DELIVERED
  );

  public OrderListResponse getUserOrders(Long userId, OrderStatus status, int page, int limit, String sortBy,
      String sortOrder) {
    User currentUser = userRepository.findById(userId)
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
    Pageable pageable = PageRequest.of(page, limit, sort);

    Page<Order> orderPage;
    if (status != null) {
      orderPage = orderRepository.findAllByUserAndStatus(currentUser, status, pageable);
    } else {
      orderPage = orderRepository.findAllByUser(currentUser, pageable);
    }

    return OrderListResponse.builder()
        .orders(orderMapper.toOrderDTOList(orderPage.getContent()))
        .pagination(OrderListResponse.PaginationInfo.builder()
            .page(orderPage.getNumber())
            .limit(orderPage.getSize())
            .totalItems(orderPage.getTotalElements())
            .totalPages(orderPage.getTotalPages())
            .hasNext(orderPage.hasNext())
            .hasPrevious(orderPage.hasPrevious())
            .build())
        .build();
  }

  public OrderDTO getOrderDetails(Long userId, Long orderId) {
    User currentUser = userRepository.findById(userId)
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    Order order = orderRepository.findByIdAndUser(orderId, currentUser)
        .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

    return orderMapper.toOrderDTO(order);
  }

  @Transactional
  public CancelOrderResponse cancelOrder(Long userId, Long orderId, CancelOrderRequest request) {
    User currentUser = userRepository.findById(userId)
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    Order order = orderRepository.findByIdAndUser(orderId, currentUser)
        .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

    if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
      throw new AppException(ErrorCode.ORDER_CANNOT_BE_CANCELLED);
    }

    order.setStatus(OrderStatus.CANCELLED);
    order.setCancelledAt(LocalDateTime.now());
    order.setCancellationReason(request.getReason());

    OrderTimeline timeline = OrderTimeline.builder()
        .order(order)
        .status("CANCELLED")
        .note("Order cancelled by user: " + request.getReason())
        .build();
    orderTimelineRepository.save(timeline);

    // Restore stock logic
    order.getItems().forEach(item -> {
      if (item.getListing() != null) {
        item.getListing().setQuantity(item.getListing().getQuantity() + item.getQuantity());
      }
    });

    orderRepository.save(order);
    
    // Process refund via Stripe
    TransactionService.RefundResult refundResult = transactionService.processRefund(order);
    
    String refundStatus = refundResult.isSuccess() ? "REFUNDED" : "PENDING";
    if (!refundResult.isSuccess()) {
      log.warn("Refund not processed for order {}: {}", orderId, refundResult.getMessage());
    }

    return CancelOrderResponse.builder()
        .orderId(order.getId())
        .status(order.getStatus())
        .refundAmount(refundResult.getAmount() != null ? refundResult.getAmount() : order.getTotal())
        .refundStatus(refundStatus)
        .refundId(refundResult.getRefundId())
        .cancelledAt(order.getCancelledAt())
        .build();
  }

  public OrderTrackingDTO getOrderTracking(Long userId, Long orderId) {
    User currentUser = userRepository.findById(userId)
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    Order order = orderRepository.findByIdAndUser(orderId, currentUser)
        .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

    return OrderTrackingDTO.builder()
        .orderId(order.getId())
        .orderNumber(order.getOrderNumber())
        .trackingNumber(order.getTrackingNumber())
        .carrier(order.getCarrier())
        .trackingUrl(order.getTrackingUrl())
        .status(order.getStatus().name())
        .estimatedDelivery(order.getEstimatedDelivery())
        .events(Collections.emptyList()) // Placeholder for tracking events logic
        .build();
  }
  
  /**
   * Update order status by seller.
   * Per Vision API_CONTRACTS.md - PATCH /orders/:orderId/status
   * 
   * Validates:
   * 1. Seller has items in this order
   * 2. Status is a valid seller transition (PROCESSING, SHIPPED, DELIVERED)
   * 3. If SHIPPED, tracking number is required
   * 
   * Creates timeline entry for audit trail.
   */
  @Transactional
  public UpdateOrderStatusResponse updateOrderStatusBySeller(Long sellerId, Long orderId, UpdateOrderStatusRequest request) {
    User seller = userRepository.findById(sellerId)
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    
    // Validate seller owns items in this order
    if (!orderItemRepository.existsByOrderAndSeller(order, seller)) {
      log.warn("Seller {} attempted to update order {} without ownership", sellerId, orderId);
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    
    // Validate status is allowed for seller
    OrderStatus newStatus = request.getStatus();
    if (!SELLER_ALLOWED_STATUSES.contains(newStatus)) {
      throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
    }
    
    // Validate tracking number for SHIPPED status
    if (newStatus == OrderStatus.SHIPPED && 
        (request.getTrackingNumber() == null || request.getTrackingNumber().isBlank())) {
      throw new AppException(ErrorCode.TRACKING_NUMBER_REQUIRED);
    }
    
    // Validate order is not cancelled
    if (order.getStatus() == OrderStatus.CANCELLED) {
      throw new AppException(ErrorCode.ORDER_ALREADY_CANCELLED);
    }
    
    // Update order
    OrderStatus previousStatus = order.getStatus();
    order.setStatus(newStatus);
    
    if (request.getTrackingNumber() != null) {
      order.setTrackingNumber(request.getTrackingNumber());
    }
    if (request.getCarrier() != null) {
      order.setCarrier(request.getCarrier());
    }
    
    // Set timestamp based on status
    LocalDateTime now = LocalDateTime.now();
    if (newStatus == OrderStatus.SHIPPED) {
      order.setShippedAt(now);
    } else if (newStatus == OrderStatus.DELIVERED) {
      order.setDeliveredAt(now);
    }
    
    orderRepository.save(order);
    
    // Create timeline entry
    String timelineNote = buildTimelineNote(previousStatus, newStatus, request);
    OrderTimeline timeline = OrderTimeline.builder()
        .order(order)
        .status(newStatus.name())
        .note(timelineNote)
        .build();
    orderTimelineRepository.save(timeline);
    
    log.info("Seller {} updated order {} status from {} to {}", 
        sellerId, orderId, previousStatus, newStatus);
    
    return UpdateOrderStatusResponse.builder()
        .id(order.getId())
        .status(order.getStatus())
        .trackingNumber(order.getTrackingNumber())
        .carrier(order.getCarrier())
        .updatedAt(now)
        .build();
  }
  
  private String buildTimelineNote(OrderStatus from, OrderStatus to, UpdateOrderStatusRequest request) {
    StringBuilder note = new StringBuilder();
    note.append("Status changed from ").append(from).append(" to ").append(to);
    
    if (to == OrderStatus.SHIPPED && request.getTrackingNumber() != null) {
      note.append(". Tracking: ").append(request.getTrackingNumber());
      if (request.getCarrier() != null) {
        note.append(" (").append(request.getCarrier()).append(")");
      }
    }
    
    if (request.getNote() != null && !request.getNote().isBlank()) {
      note.append(". ").append(request.getNote());
    }
    
    return note.toString();
  }
}
