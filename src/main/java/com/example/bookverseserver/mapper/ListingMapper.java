package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Product.ListingDeleteRequest;
import com.example.bookverseserver.dto.request.Product.ListingRequest;
import com.example.bookverseserver.dto.request.Product.ListingUpdateRequest;
import com.example.bookverseserver.dto.response.Product.ListingResponse;
import com.example.bookverseserver.dto.response.Product.ListingUpdateResponse;
import com.example.bookverseserver.entity.Product.Listing;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ListingMapper {
    Listing toListing(ListingRequest request);

    ListingResponse toListingResponse(Listing listing);

//    @Mapping(target = "id", source = "id")
//    @Mapping(target = "titleOverride", source = "titleOverride")
//    @Mapping(target = "price", source = "price")
//    @Mapping(target = "currency", source = "currency")
//    @Mapping(target = "condition", source = "condition")
//    @Mapping(target = "quantity", source = "quantity")
//    @Mapping(target = "location", source = "location")
//    @Mapping(target = "status", source = "status")
//    @Mapping(target = "visibility", source = "visibility")
//    @Mapping(target = "platformFeePercent", source = "platformFeePercent")
//    @Mapping(target = "suggestedPriceLow", source = "suggestedPriceLow")
//    @Mapping(target = "suggestedPriceHigh", source = "suggestedPriceHigh")
//    @Mapping(target = "views", source = "views")
//    @Mapping(target = "likes", source = "likes")
//    @Mapping(target = "soldCount", source = "soldCount")
    @Mapping(target = "createdAt", source = "createdAt")
//    @Mapping(target = "updatedAt", source = "updatedAt")
    ListingUpdateResponse toListingUpdateResponse(Listing listing);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateListing(@MappingTarget Listing listing, ListingUpdateRequest request);
}
