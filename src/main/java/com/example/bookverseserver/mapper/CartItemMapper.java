package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.CartItem.CartItemRequest;
import com.example.bookverseserver.dto.response.CartItem.CartItemForCartResponse;
import com.example.bookverseserver.dto.response.CartItem.CartItemResponse;
import com.example.bookverseserver.entity.Order_Payment.Cart;
import com.example.bookverseserver.entity.Order_Payment.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    CartItemRequest toResponse(CartItem cartItem);

    CartItemResponse toCartItemResponse(CartItem cartItem);

    @Mapping(target = "listingId", expression = "java(cartItem.getListing().getId())")
    @Mapping(target = "title", expression = "java(cartItem.getListing().getTitleOverride() != null ? cartItem.getListing().getTitleOverride() : cartItem.getListing().getBookMeta().getTitle())")
    @Mapping(target = "subTotalPrice", expression = "java(cartItem.getSubtotalPrice())")
    CartItemForCartResponse toCartItemForCartResponse(CartItem cartItem);
}
