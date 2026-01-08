package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Product.ListingRequest;
import com.example.bookverseserver.dto.request.Product.ListingUpdateRequest;
import com.example.bookverseserver.dto.response.Product.*;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.Product.BookMeta;
import com.example.bookverseserver.entity.Product.ListingPhoto;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.entity.User.UserProfile;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class ListingMapper {

    @Autowired
    protected AuthorMapper authorMapper;

    @Autowired
    protected CategoryMapper categoryMapper;

    @Mapping(source = "condition", target = "condition")
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "soldCount", ignore = true)
    public abstract Listing toListing(ListingRequest request);

    /**
     * Map listing to listing response with nested structure per Vision API_CONTRACTS.md.
     */
    public ListingResponse toListingResponse(Listing listing) {
        if (listing == null) {
            return null;
        }

        BookMeta bookMeta = listing.getBookMeta();
        User seller = listing.getSeller();
        UserProfile profile = seller != null ? seller.getUserProfile() : null;
        
        return ListingResponse.builder()
                .id(listing.getId())
                // Nested book info
                .book(ListingResponse.BookInfo.builder()
                        .id(bookMeta != null ? bookMeta.getId() : null)
                        .title(bookMeta != null ? bookMeta.getTitle() : null)
                        .authors(bookMeta != null && bookMeta.getAuthors() != null 
                            ? bookMeta.getAuthors().stream()
                                .map(authorMapper::toAuthorResponse)
                                .collect(Collectors.toList())
                            : List.of())
                        .isbn(bookMeta != null ? bookMeta.getIsbn() : null)
                        .coverImage(bookMeta != null ? bookMeta.getCoverImageUrl() : null)
                        .build())
                .category(listing.getCategory() != null ? categoryMapper.toCategoryResponse(listing.getCategory()) : null)
                // Nested seller info
                .seller(ListingResponse.SellerInfo.builder()
                        .id(seller != null ? seller.getId() : null)
                        .name(profile != null ? profile.getDisplayName() : (seller != null ? seller.getUsername() : null))
                        .avatar(profile != null ? profile.getAvatarUrl() : null)
                        .isPro(profile != null ? profile.getIsProSeller() : false)
                        .rating(profile != null && profile.getRatingAvg() != null 
                            ? java.math.BigDecimal.valueOf(profile.getRatingAvg()) : null)
                        .build())
                .price(listing.getPrice())
                .originalPrice(listing.getOriginalPrice())
                .finalPrice(listing.getPrice()) // finalPrice = price (current selling price per Vision)
                .condition(listing.getCondition())
                .quantity(listing.getQuantity())
                // Photos as URL strings only
                .photos(extractPhotoUrls(listing.getPhotos()))
                // Nested stats
                .stats(ListingResponse.ListingStats.builder()
                        .views(listing.getViews())
                        .favorites(listing.getLikes())  // "likes" → "favorites" per Vision
                        .soldCount(listing.getSoldCount())
                        .build())
                .createdAt(listing.getCreatedAt() != null ? listing.getCreatedAt().toString() : null)
                .build();
    }

    /**
     * Map ListingPhoto entity to response DTO.
     */
    private ListingPhotoResponse toListingPhotoResponse(ListingPhoto photo) {
        if (photo == null) {
            return null;
        }
        return ListingPhotoResponse.builder()
                .id(photo.getId())
                .url(photo.getUrl())
                .position(photo.getPosition())
                .createdAt(photo.getCreatedAt())
                .build();
    }

    public abstract ListingUpdateResponse toListingUpdateResponse(Listing listing);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateListing(@MappingTarget Listing listing, ListingUpdateRequest request);

    // ============ New mapping methods for detailed responses ============

    /**
     * Map listing to detailed response for single listing view.
     */
    public ListingDetailResponse toDetailResponse(Listing listing) {
        if (listing == null) {
            return null;
        }

        return ListingDetailResponse.builder()
                .id(listing.getId()) // Sử dụng Long trực tiếp
                .book(toBookSummary(listing.getBookMeta()))
                .category(listing.getCategory() != null ? categoryMapper.toCategoryResponse(listing.getCategory()) : null)
                .seller(toSellerSummary(listing.getSeller()))
                .condition(listing.getCondition())
                .price(listing.getPrice())
                .originalPrice(listing.getOriginalPrice())
                .finalPrice(listing.getPrice()) // finalPrice = price (current selling price per Vision)
                .discount(listing.getDiscountPercentage())
                .quantity(listing.getQuantity())
                .status(listing.getStatus())
                .description(listing.getDescription())
                .photos(extractPhotoUrls(listing.getPhotos()))
                .shippingInfo(toShippingInfo(listing))
                .views(listing.getViews())
                .soldCount(listing.getSoldCount())
                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .build();
    }

    /**
     * Map BookMeta to summary DTO.
     */
    public BookSummaryDto toBookSummary(BookMeta bookMeta) {
        if (bookMeta == null) {
            return null;
        }

        String authorName = null;
        if (bookMeta.getAuthors() != null && !bookMeta.getAuthors().isEmpty()) {
            authorName = bookMeta.getAuthors().iterator().next().getName();
        }

        return BookSummaryDto.builder()
                .id(bookMeta.getId()) // Sử dụng Long trực tiếp
                .title(bookMeta.getTitle())
                .author(authorName)
                .authors(bookMeta.getAuthors() != null 
                    ? bookMeta.getAuthors().stream()
                        .map(authorMapper::toAuthorResponse)
                        .collect(Collectors.toList())
                    : List.of())
                .isbn(bookMeta.getIsbn())
                .coverImage(bookMeta.getCoverImageUrl())
                .averageRating(bookMeta.getAverageRating())
                .totalReviews(bookMeta.getTotalReviews())
                .build();
    }

    /**
     * Map User to seller summary DTO.
     */
    public SellerSummaryDto toSellerSummary(User seller) {
        if (seller == null) {
            return null;
        }

        UserProfile profile = seller.getUserProfile();

        return SellerSummaryDto.builder()
                .id(seller.getId()) // Sử dụng Long trực tiếp
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
    public ShippingInfoDto toShippingInfo(Listing listing) {
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
    public RelatedListingDto toRelatedDto(Listing listing) {
        if (listing == null) {
            return null;
        }

        String mainPhotoUrl = null;
        if (listing.getPhotos() != null && !listing.getPhotos().isEmpty()) {
            mainPhotoUrl = listing.getPhotos().get(0).getUrl();
        }

        return RelatedListingDto.builder()
                .id(listing.getId()) // Sử dụng Long trực tiếp
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
    public List<String> extractPhotoUrls(List<ListingPhoto> photos) {
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
    public List<RelatedListingDto> toRelatedDtoList(List<Listing> listings) {
        if (listings == null) {
            return List.of();
        }
        return listings.stream()
                .map(this::toRelatedDto)
                .collect(Collectors.toList());
    }
}