package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.response.Book.AuthorResponse;
import com.example.bookverseserver.dto.response.Book.BookResponse;
import com.example.bookverseserver.dto.response.Book.CategoryResponse;
import com.example.bookverseserver.dto.response.CuratedCollection.CuratedCollectionDetailResponse;
import com.example.bookverseserver.dto.response.CuratedCollection.CuratedCollectionSummaryResponse;
import com.example.bookverseserver.entity.Product.BookMeta;
import com.example.bookverseserver.entity.Product.CuratedCollection;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.enums.ListingStatus;
import com.example.bookverseserver.repository.ListingRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class CuratedCollectionMapper {

    @Autowired
    protected ListingRepository listingRepository;

    private static final int COVER_IMAGE_COUNT = 3;

    @Mapping(target = "id", source = "id")
    @Mapping(target = "coverImageUrls", expression = "java(getRandomCoverImages(collection.getBooks()))")
    public abstract CuratedCollectionSummaryResponse toSummaryResponse(CuratedCollection collection);

    @Mapping(target = "totalBooks", expression = "java(collection.getTotalBooks())")
    @Mapping(target = "coverImageUrls", expression = "java(getRandomCoverImages(collection.getBooks()))")
    @Mapping(target = "books", expression = "java(mapBooksToResponses(collection.getBooks()))")
    public abstract CuratedCollectionDetailResponse toDetailResponse(CuratedCollection collection);

    public List<CuratedCollectionSummaryResponse> toSummaryResponseList(List<CuratedCollection> collections) {
        if (collections == null) {
            return List.of();
        }
        return collections.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    protected List<String> getRandomCoverImages(Set<BookMeta> books) {
        if (books == null || books.isEmpty()) {
            return List.of();
        }

        List<String> coverUrls = books.stream()
                .map(BookMeta::getCoverImageUrl)
                .filter(Objects::nonNull)
                .filter(url -> !url.isEmpty())
                .collect(Collectors.toList());

        if (coverUrls.isEmpty()) {
            return List.of();
        }

        // Shuffle to get random selection
        Collections.shuffle(coverUrls);
        return coverUrls.stream()
                .limit(COVER_IMAGE_COUNT)
                .collect(Collectors.toList());
    }

    protected List<BookResponse> mapBooksToResponses(Set<BookMeta> books) {
        if (books == null || books.isEmpty()) {
            return List.of();
        }

        return books.stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    private BookResponse convertToBookResponse(BookMeta bookMeta) {
        BookResponse bookResponse = new BookResponse();
        bookResponse.setId(bookMeta.getId());
        bookResponse.setTitle(bookMeta.getTitle());
        bookResponse.setIsbn(bookMeta.getIsbn());

        bookResponse.setAuthors(bookMeta.getAuthors() != null ? bookMeta.getAuthors().stream()
                .map(author -> new AuthorResponse(author.getId(), author.getName()))
                .collect(Collectors.toList()) : List.of());

        bookResponse.setCategories(bookMeta.getCategories() != null ? bookMeta.getCategories().stream()
                .map(category -> new CategoryResponse(category.getId(), category.getName()))
                .collect(Collectors.toList()) : List.of());

        bookResponse.setCover_url(bookMeta.getCoverImageUrl());

        List<Listing> listings = listingRepository.findByBookMetaAndStatusAndVisibility(
                bookMeta, ListingStatus.ACTIVE, true);

        if (!listings.isEmpty()) {
            Listing cheapestListing = listings.stream()
                    .min(Comparator.comparing(Listing::getPrice))
                    .orElse(null);

            if (cheapestListing != null) {
                Map<String, Object> cheapestListingPreview = new HashMap<>();
                cheapestListingPreview.put("listing_id", cheapestListing.getId().toString());
                cheapestListingPreview.put("price", cheapestListing.getPrice().toString());
                cheapestListingPreview.put("currency", cheapestListing.getCurrency());
                bookResponse.setCheapest_listing_preview(cheapestListingPreview);
            }
        }

        return bookResponse;
    }
}
