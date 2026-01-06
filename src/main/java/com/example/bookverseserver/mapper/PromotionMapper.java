package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.response.Promotion.PromotionResponse;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.Product.Promotion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PromotionMapper {
    
    @Mapping(source = "appliedListings", target = "appliedBooks", qualifiedByName = "listingsToIds")
    PromotionResponse toResponse(Promotion promotion);
    
    List<PromotionResponse> toResponseList(List<Promotion> promotions);
    
    @Named("listingsToIds")
    default List<Long> listingsToIds(Set<Listing> listings) {
        if (listings == null) return List.of();
        return listings.stream()
                .map(Listing::getId)
                .collect(Collectors.toList());
    }
}
