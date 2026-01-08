package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.ShippingAddress.ShippingAddressRequest;
import com.example.bookverseserver.dto.response.ShippingAddress.ShippingAddressResponse;
import com.example.bookverseserver.entity.User.ShippingAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper for shipping addresses - aligned with Vision/FE ShippingAddressData
 */
@Mapper(componentModel = "spring")
public interface ShippingAddressMapper {

    ShippingAddress toShippingAddress(ShippingAddressRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "addressLine1", source = "addressLine1")
    @Mapping(target = "addressLine2", source = "addressLine2")
    @Mapping(target = "postalCode", source = "postalCode")
    ShippingAddressResponse toShippingAddressResponse(ShippingAddress shippingAddress);

    void updateShippingAddress(@MappingTarget ShippingAddress shippingAddress, ShippingAddressRequest request);
}


