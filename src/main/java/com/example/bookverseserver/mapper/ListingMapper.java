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

    @Mapping(source = "bookMeta.id", target = "bookMetaId")
    @Mapping(source = "bookMeta.title", target = "bookTitle")
    @Mapping(source = "seller.id", target = "sellerId")
    @Mapping(source = "seller.username", target = "sellerName")
    ListingResponse toListingResponse(Listing listing);

    ListingUpdateResponse toListingUpdateResponse(Listing listing);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateListing(@MappingTarget Listing listing, ListingUpdateRequest request);
}
