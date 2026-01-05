package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.ShippingAddress.ShippingAddressRequest;
import com.example.bookverseserver.dto.response.ShippingAddress.ShippingAddressResponse;
import com.example.bookverseserver.entity.User.ShippingAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ShippingAddressMapper {

    ShippingAddress toShippingAddress(ShippingAddressRequest request);

    @Mapping(target = "phone", source = "phoneNumber")
    @Mapping(target = "street", expression = "java(combineAddressLines(shippingAddress))")
    @Mapping(target = "zipCode", source = "postalCode")
    @Mapping(target = "state", source = "district")
    ShippingAddressResponse toShippingAddressResponse(ShippingAddress shippingAddress);

    void updateShippingAddress(@MappingTarget ShippingAddress shippingAddress, ShippingAddressRequest request);

    default String combineAddressLines(ShippingAddress address) {
        StringBuilder street = new StringBuilder();
        if (address.getAddressLine1() != null) {
            street.append(address.getAddressLine1());
        }
        if (address.getAddressLine2() != null) {
            if (street.length() > 0) street.append(", ");
            street.append(address.getAddressLine2());
        }
        return street.length() > 0 ? street.toString() : null;
    }
}


