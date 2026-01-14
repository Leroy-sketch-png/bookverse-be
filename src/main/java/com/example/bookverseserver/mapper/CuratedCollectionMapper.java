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

import java.math.BigDecimal;
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

        bookResponse.setCoverUrl(bookMeta.getCoverImageUrl());

        // MARKETPLACE AGGREGATION — price range and seller count
        List<Listing> listings = listingRepository.findByBookMetaAndStatusAndVisibility(
                bookMeta, ListingStatus.ACTIVE, true);
        bookResponse.setTotalListings(listings.size());

        if (!listings.isEmpty()) {
            BigDecimal minPrice = listings.stream()
                    .map(Listing::getPrice)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            BigDecimal maxPrice = listings.stream()
                    .map(Listing::getPrice)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            String currency = listings.get(0).getCurrency();
            
            bookResponse.setMinPrice(minPrice);
            bookResponse.setMaxPrice(maxPrice);
            bookResponse.setCurrency(currency);
        }
        
        // Book-level ratings
        bookResponse.setAverageRating(bookMeta.getAverageRating());
        bookResponse.setTotalReviews(bookMeta.getTotalReviews());

        return bookResponse;
    }
}
