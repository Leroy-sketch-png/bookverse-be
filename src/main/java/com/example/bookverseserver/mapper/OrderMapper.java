package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.response.Order.OrderDTO;
import com.example.bookverseserver.dto.response.Order.OrderItemDTO;
import com.example.bookverseserver.dto.response.Order.OrderTimelineDTO;
import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.Order_Payment.OrderItem;
import com.example.bookverseserver.entity.Order_Payment.OrderTimeline;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
  @Mapping(target = "id", expression = "java(order.getId().toString())")
  @Mapping(target = "items", qualifiedByName = "toOrderItemDTO") // Use the default method
  @Mapping(target = "paymentStatus", ignore = true) // Handle in service or via helper
  @Mapping(target = "paymentMethod", ignore = true) // Placeholder
  OrderDTO toOrderDTO(Order order);

  // Internal method - DO NOT use directly
  @Mapping(target = "id", expression = "java(orderItem.getId().toString())")
  @Mapping(target = "bookId", expression = "java(String.valueOf(orderItem.getBookMeta().getId()))")
  @Mapping(target = "seller", ignore = true) // Map manually in default method
  @Mapping(target = "title", source = "bookMeta.title")
  @Mapping(target = "author", expression = "java(orderItem.getBookMeta().getAuthors().isEmpty() ? null : orderItem.getBookMeta().getAuthors().iterator().next().getName())")
  @Mapping(target = "coverImage", expression = "java(orderItem.getBookMeta().getCoverImageUrl())")
  OrderItemDTO mapOrderItemBasic(OrderItem orderItem);

  @org.mapstruct.Named("toOrderItemDTO")
  default OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
    if (orderItem == null) return null;

    OrderItemDTO dto = mapOrderItemBasic(orderItem);

    // Manually map seller info
    if (orderItem.getSeller() != null) {
      OrderItemDTO.SellerInfo sellerInfo = OrderItemDTO.SellerInfo.builder()
        .id(String.valueOf(orderItem.getSeller().getId()))
        .name(orderItem.getSeller().getUsername())
        .slug(null) // TODO: Add slug if available
        .build();
      dto.setSeller(sellerInfo);
    }

    return dto;
  }

  @Mapping(source = "createdAt", target = "timestamp")
  OrderTimelineDTO toOrderTimelineDTO(OrderTimeline timeline);

  List<OrderDTO> toOrderDTOList(List<Order> orders);
}
