package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.response.PayoutResponse;
import com.example.bookverseserver.entity.Order_Payment.SellerPayout;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PayoutMapper {

    @Mapping(source = "seller.id", target = "sellerId")
    @Mapping(source = "seller.username", target = "sellerUsername")
    @Mapping(source = "seller.userProfile.displayName", target = "sellerDisplayName")
    @Mapping(source = "seller.email", target = "sellerEmail")
    PayoutResponse toResponse(SellerPayout payout);

    List<PayoutResponse> toResponseList(List<SellerPayout> payouts);
}
