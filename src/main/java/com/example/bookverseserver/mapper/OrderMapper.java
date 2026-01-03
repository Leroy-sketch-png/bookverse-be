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
  @Mapping(target = "paymentStatus", ignore = true) // Handle in service or via helper
  @Mapping(target = "paymentMethod", source = "id", ignore = true) // Placeholder
  OrderDTO toOrderDTO(Order order);

  @Mapping(source = "bookMeta.id", target = "bookId")
  @Mapping(source = "seller.id", target = "seller.id")
  @Mapping(source = "seller.username", target = "seller.name")
  @Mapping(target = "seller.slug", ignore = true) // Placeholder or implement if slug exists
  OrderItemDTO toOrderItemDTO(OrderItem orderItem);

  @Mapping(source = "createdAt", target = "timestamp")
  OrderTimelineDTO toOrderTimelineDTO(OrderTimeline timeline);

  List<OrderDTO> toOrderDTOList(List<Order> orders);
}
