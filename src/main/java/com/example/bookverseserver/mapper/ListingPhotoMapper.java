package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Product.ListingPhotoRequest;
import com.example.bookverseserver.dto.response.Product.ListingPhotoResponse;
import com.example.bookverseserver.entity.Product.ListingPhoto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ListingPhotoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "listing", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ListingPhoto toListingPhoto(ListingPhotoRequest request);
    ListingPhotoResponse toListingPhotoResponse(ListingPhoto entity);
}
