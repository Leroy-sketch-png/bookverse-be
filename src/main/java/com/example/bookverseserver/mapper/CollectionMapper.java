package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.response.Product.ListingResponse;
import com.example.bookverseserver.dto.response.Wishlist.CollectionResponse;
import com.example.bookverseserver.entity.Product.Collection;
import com.example.bookverseserver.entity.Product.Listing;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {ListingMapper.class})
public abstract class CollectionMapper {

    @Autowired
    protected ListingMapper listingMapper;

    @Mapping(target = "id", source = "id")
    @Mapping(target = "books", expression = "java(mapListingsToBookResponses(collection.getListings()))")
    @Mapping(target = "totalBooks", expression = "java(collection.getListings() != null ? collection.getListings().size() : 0)")
    public abstract CollectionResponse toCollectionResponse(Collection collection);

    protected List<ListingResponse> mapListingsToBookResponses(java.util.Set<Listing> listings) {
        if (listings == null) {
            return List.of();
        }
        return listings.stream()
                .map(listingMapper::toListingResponse)
                .collect(Collectors.toList());
    }

    public List<CollectionResponse> toCollectionResponseList(List<Collection> collections) {
        if (collections == null) {
            return List.of();
        }
        return collections.stream()
                .map(this::toCollectionResponse)
                .collect(Collectors.toList());
    }
}
