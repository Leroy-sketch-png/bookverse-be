package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.response.Order.OrderDTO;
import com.example.bookverseserver.dto.response.Order.OrderItemDTO;
import com.example.bookverseserver.dto.response.Order.OrderTimelineDTO;
import com.example.bookverseserver.dto.response.ShippingAddress.ShippingAddressResponse;
import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.Order_Payment.OrderItem;
import com.example.bookverseserver.entity.Order_Payment.OrderTimeline;
import com.example.bookverseserver.entity.Order_Payment.Payment;
import com.example.bookverseserver.entity.User.ShippingAddress;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.entity.User.UserProfile;
import com.example.bookverseserver.enums.PaymentStatus;
import org.mapstruct.Mapper;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order Mapper - builds Vision-aligned OrderDTO with nested structures
 */
@Mapper(componentModel = "spring", uses = {ShippingAddressMapper.class})
public interface OrderMapper {

  /**
   * Map Order entity to OrderDTO (Vision structure)
   * Uses default method for complex nested mapping
   */
  default OrderDTO toOrderDTO(Order order) {
    if (order == null) return null;
    
    // Get seller from first order item (marketplace: one seller per order typically)
    User seller = null;
    if (order.getItems() != null && !order.getItems().isEmpty()) {
      seller = order.getItems().get(0).getSeller();
    }
    
    // Build seller info
    OrderDTO.SellerInfo sellerInfo = null;
    if (seller != null) {
      UserProfile profile = seller.getUserProfile();
      sellerInfo = OrderDTO.SellerInfo.builder()
          .id(seller.getId())
          .name(profile != null ? profile.getFullName() : seller.getUsername())
          .avatar(profile != null ? profile.getAvatarUrl() : null)
          .build();
    }
    
    // Build order items
    List<OrderItemDTO> items = order.getItems() != null 
        ? order.getItems().stream().map(this::toOrderItemDTO).collect(Collectors.toList())
        : List.of();
    
    // Build summary (Vision: nested financial object)
    OrderDTO.OrderSummary summary = OrderDTO.OrderSummary.builder()
        .subtotal(order.getSubtotal())
        .shipping(order.getShipping())
        .discount(order.getDiscount())
        .total(order.getTotal())
        .build();
    
    // Build shipping info (Vision: nested shipping object)
    OrderDTO.ShippingInfo shippingInfo = OrderDTO.ShippingInfo.builder()
        .address(mapShippingAddress(order.getShippingAddress()))
        .carrier(order.getCarrier())
        .trackingNumber(order.getTrackingNumber())
        .trackingUrl(order.getTrackingUrl())
        .build();
    
    // Build timeline
    List<OrderTimelineDTO> timeline = order.getTimeline() != null
        ? order.getTimeline().stream().map(this::toOrderTimelineDTO).collect(Collectors.toList())
        : List.of();
    
    // Get payment info from payments list (first payment)
    String paymentMethod = "stripe";
    PaymentStatus paymentStatus = PaymentStatus.PENDING;
    if (order.getPayments() != null && !order.getPayments().isEmpty()) {
      Payment payment = order.getPayments().get(0);
      paymentMethod = payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "stripe";
      paymentStatus = payment.getStatus() != null ? payment.getStatus() : PaymentStatus.PENDING;
    }
    
    return OrderDTO.builder()
        .id(order.getId())
        .orderNumber(order.getOrderNumber())
        .status(order.getStatus())
        .seller(sellerInfo)
        .items(items)
        .summary(summary)
        .paymentMethod(paymentMethod)
        .paymentStatus(paymentStatus)
        .shipping(shippingInfo)
        .timeline(timeline)
        .estimatedDelivery(order.getEstimatedDelivery() != null 
            ? order.getEstimatedDelivery().format(DateTimeFormatter.ISO_DATE) 
            : null)
        .createdAt(order.getCreatedAt())
        .updatedAt(order.getUpdatedAt())
        .build();
  }

  // Map order item with nested listing structure (Vision aligned)
  default OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
    if (orderItem == null) return null;

    // Build nested book info
    OrderItemDTO.BookInfo bookInfo = OrderItemDTO.BookInfo.builder()
        .title(orderItem.getTitle())
        .coverImage(orderItem.getCoverImage())
        .build();
    
    // Build nested listing info
    OrderItemDTO.ListingInfo listingInfo = OrderItemDTO.ListingInfo.builder()
        .id(orderItem.getListing() != null ? orderItem.getListing().getId() : null)
        .book(bookInfo)
        .condition(orderItem.getListing() != null ? orderItem.getListing().getCondition() : null)
        .build();

    return OrderItemDTO.builder()
        .id(orderItem.getId())
        .listing(listingInfo)
        .quantity(orderItem.getQuantity())
        .priceAtPurchase(orderItem.getPrice())
        .build();
  }

  // Map timeline entry
  default OrderTimelineDTO toOrderTimelineDTO(OrderTimeline timeline) {
    if (timeline == null) return null;
    return OrderTimelineDTO.builder()
        .status(timeline.getStatus())
        .timestamp(timeline.getCreatedAt() != null 
            ? timeline.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME) 
            : null)
        .note(timeline.getNote())
        .build();
  }

  // Map shipping address to Vision-aligned response
  default ShippingAddressResponse mapShippingAddress(ShippingAddress address) {
    if (address == null) return null;
    return ShippingAddressResponse.builder()
        .id(address.getId())
        .userId(address.getUser() != null ? address.getUser().getId() : null)
        .fullName(address.getFullName())
        .phoneNumber(address.getPhoneNumber())
        .addressLine1(address.getAddressLine1())
        .addressLine2(address.getAddressLine2())
        .city(address.getCity())
        .postalCode(address.getPostalCode())
        .country(address.getCountry())
        .ward(address.getWard())
        .district(address.getDistrict())
        .note(address.getNote())
        .isDefault(address.getIsDefault())
        .createdAt(address.getCreatedAt())
        .updatedAt(address.getUpdatedAt())
        .build();
  }

  // List mapping
  default List<OrderDTO> toOrderDTOList(List<Order> orders) {
    if (orders == null) return List.of();
    return orders.stream().map(this::toOrderDTO).collect(Collectors.toList());
  }
}
