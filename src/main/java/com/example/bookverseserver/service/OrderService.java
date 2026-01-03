package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Order.CancelOrderRequest;
import com.example.bookverseserver.dto.response.Order.CancelOrderResponse;
import com.example.bookverseserver.dto.response.Order.OrderDTO;
import com.example.bookverseserver.dto.response.Order.OrderListResponse;
import com.example.bookverseserver.dto.response.Order.OrderTrackingDTO;
import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.Order_Payment.OrderTimeline;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.OrderStatus;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.OrderMapper;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderService {

  OrderRepository orderRepository;
  OrderTimelineRepository orderTimelineRepository;
  UserRepository userRepository;
  OrderMapper orderMapper;

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

  public OrderDTO getOrderDetails(Long userId, UUID orderId) {
    User currentUser = userRepository.findById(userId)
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    Order order = orderRepository.findByIdAndUser(orderId, currentUser)
        .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

    return orderMapper.toOrderDTO(order);
  }

  @Transactional
  public CancelOrderResponse cancelOrder(Long userId, UUID orderId, CancelOrderRequest request) {
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

    return CancelOrderResponse.builder()
        .orderId(order.getId())
        .status(order.getStatus())
        .refundAmount(order.getTotal())
        .refundStatus("PENDING")
        .cancelledAt(order.getCancelledAt())
        .build();
  }

  public OrderTrackingDTO getOrderTracking(Long userId, UUID orderId) {
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
}
