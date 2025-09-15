package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Product.ListingPhotoRequest;
//import com.example.bookverseserver.dto.response.Product.ListingPhotoResponse;
import com.example.bookverseserver.dto.response.Product.ListingPhotoResponse;
import com.example.bookverseserver.entity.Product.ListingPhoto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ListingPhotoMapper {
    ListingPhoto toListingPhoto(ListingPhotoRequest request);
    ListingPhotoResponse toListingPhotoResponse(ListingPhoto entity);
}
