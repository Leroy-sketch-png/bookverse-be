package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Product.ListingDeleteRequest;
import com.example.bookverseserver.dto.request.Product.ListingRequest;
import com.example.bookverseserver.dto.request.Product.ListingUpdateRequest;
import com.example.bookverseserver.dto.response.Product.*;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.Product.BookMeta;
import com.example.bookverseserver.entity.Product.ListingPhoto;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.entity.User.UserProfile;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ListingMapper {

    @Mapping(source = "condition", target = "condition")
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "soldCount", ignore = true)
    Listing toListing(ListingRequest request);

    @Mapping(source = "bookMeta.id", target = "bookMetaId")
    @Mapping(source = "bookMeta.title", target = "bookTitle")
    @Mapping(source = "seller.id", target = "sellerId")
    @Mapping(source = "seller.username", target = "sellerName")
    ListingResponse toListingResponse(Listing listing);

    ListingUpdateResponse toListingUpdateResponse(Listing listing);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateListing(@MappingTarget Listing listing, ListingUpdateRequest request);

    // ============ New mapping methods for detailed responses ============

    /**
     * Map listing to detailed response for single listing view.
     */
    default ListingDetailResponse toDetailResponse(Listing listing) {
        if (listing == null) {
            return null;
        }

        return ListingDetailResponse.builder()
                .id(listing.getId())
                .book(toBookSummary(listing.getBookMeta()))
                .seller(toSellerSummary(listing.getSeller()))
                .condition(listing.getCondition())
                .price(listing.getPrice())
                .originalPrice(listing.getOriginalPrice())
                .discount(listing.getDiscountPercentage())
                .stockQuantity(listing.getQuantity())
                .status(listing.getStatus())
                .description(listing.getDescription())
                .images(extractPhotoUrls(listing.getPhotos()))
                .shippingInfo(toShippingInfo(listing))
                .viewCount(listing.getViews())
                .soldCount(listing.getSoldCount())
                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .build();
    }

    /**
     * Map BookMeta to summary DTO.
     */
    default BookSummaryDto toBookSummary(BookMeta bookMeta) {
        if (bookMeta == null) {
            return null;
        }

        String authorName = null;
        if (bookMeta.getAuthors() != null && !bookMeta.getAuthors().isEmpty()) {
            authorName = bookMeta.getAuthors().iterator().next().getName();
        }

        return BookSummaryDto.builder()
                .id(bookMeta.getId())
                .title(bookMeta.getTitle())
                .author(authorName)
                .isbn(bookMeta.getIsbn())
                .coverImage(bookMeta.getCoverImageUrl())
                .averageRating(bookMeta.getAverageRating())
                .totalReviews(bookMeta.getTotalReviews())
                .build();
    }

    /**
     * Map User to seller summary DTO.
     */
    default SellerSummaryDto toSellerSummary(User seller) {
        if (seller == null) {
            return null;
        }

        UserProfile profile = seller.getUserProfile();

        return SellerSummaryDto.builder()
                .id(seller.getId())
                .username(seller.getUsername())
                .businessName(profile != null ? profile.getDisplayName() : seller.getUsername())
                .avatar(profile != null ? profile.getAvatarUrl() : null)
                .rating(profile != null ? profile.getRatingAvg() : 0.0)
                .totalReviews(profile != null ? profile.getRatingCount() : 0)
                .isProSeller(profile != null ? profile.getIsProSeller() : false)
                .memberSince(seller.getCreatedAt())
                .build();
    }

    /**
     * Map listing shipping fields to DTO.
     */
    default ShippingInfoDto toShippingInfo(Listing listing) {
        if (listing == null) {
            return null;
        }

        return ShippingInfoDto.builder()
                .freeShipping(listing.getFreeShipping())
                .estimatedDays(listing.getEstimatedShippingDays())
                .shipsFrom(listing.getShipsFrom())
                .build();
    }

    /**
     * Map listing to related listing summary.
     */
    default RelatedListingDto toRelatedDto(Listing listing) {
        if (listing == null) {
            return null;
        }

        String mainPhotoUrl = null;
        if (listing.getPhotos() != null && !listing.getPhotos().isEmpty()) {
            mainPhotoUrl = listing.getPhotos().get(0).getUrl();
        }

        return RelatedListingDto.builder()
                .id(listing.getId())
                .seller(toSellerSummary(listing.getSeller()))
                .condition(listing.getCondition())
                .price(listing.getPrice())
                .stockQuantity(listing.getQuantity())
                .mainPhotoUrl(mainPhotoUrl)
                .build();
    }

    /**
     * Extract photo URLs from listing photos.
     */
    default List<String> extractPhotoUrls(List<ListingPhoto> photos) {
        if (photos == null) {
            return List.of();
        }
        return photos.stream()
                .map(ListingPhoto::getUrl)
                .collect(Collectors.toList());
    }

    /**
     * Map list of listings to related listing DTOs.
     */
    default List<RelatedListingDto> toRelatedDtoList(List<Listing> listings) {
        if (listings == null) {
            return List.of();
        }
        return listings.stream()
                .map(this::toRelatedDto)
                .collect(Collectors.toList());
    }
}
