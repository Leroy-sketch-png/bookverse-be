package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Product.ListingRequest;
import com.example.bookverseserver.dto.request.Product.ListingUpdateRequest;
import com.example.bookverseserver.dto.response.Product.ListingResponse;
import com.example.bookverseserver.dto.response.Product.ListingUpdateResponse;
import com.example.bookverseserver.entity.Product.BookMeta;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.BookCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ListingMapperTest {

    private ListingMapper listingMapper;

    @BeforeEach
    void setUp() {
        listingMapper = new ListingMapperImpl();
        // ListingMapper depends on these mappers
        ReflectionTestUtils.setField(listingMapper, "authorMapper", new AuthorMapperImpl());
        ReflectionTestUtils.setField(listingMapper, "categoryMapper", new CategoryMapperImpl());
    }

    @Test
    void testToListing() {
        ListingRequest request = new ListingRequest();
        request.setPrice(BigDecimal.valueOf(100));
        request.setCondition(BookCondition.NEW);

        Listing listing = listingMapper.toListing(request);

        assertThat(listing).isNotNull();
        assertThat(listing.getPrice()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(listing.getCondition()).isEqualTo(BookCondition.NEW);
        assertThat(listing.getLikes()).isZero();
        assertThat(listing.getViews()).isZero();
        assertThat(listing.getSoldCount()).isZero();
    }

    @Test
    void testToListingResponse() {
        Listing listing = new Listing();
        listing.setId(1L);
        listing.setPrice(BigDecimal.valueOf(100));

        BookMeta bookMeta = new BookMeta();
        bookMeta.setId(2L);
        bookMeta.setTitle("Test Book");
        listing.setBookMeta(bookMeta);

        User seller = new User();
        seller.setId(3L);
        seller.setUsername("test_seller");
        listing.setSeller(seller);

        ListingResponse response = listingMapper.toListingResponse(listing);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getPrice()).isEqualTo(BigDecimal.valueOf(100));
        // Nested structure per Vision API_CONTRACTS.md
        assertThat(response.getBook()).isNotNull();
        assertThat(response.getBook().getId()).isEqualTo(2L);
        assertThat(response.getBook().getTitle()).isEqualTo("Test Book");
        assertThat(response.getSeller()).isNotNull();
        assertThat(response.getSeller().getId()).isEqualTo(3L);
        assertThat(response.getSeller().getName()).isEqualTo("test_seller");
    }

    @Test
    void testToListingUpdateResponse() {
        Listing listing = new Listing();
        listing.setId(1L);
        listing.setPrice(BigDecimal.valueOf(120));

        ListingUpdateResponse response = listingMapper.toListingUpdateResponse(listing);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getPrice()).isEqualTo(BigDecimal.valueOf(120));
    }

    @Test
    void testUpdateListing() {
        ListingUpdateRequest request = new ListingUpdateRequest();
        request.setPrice(BigDecimal.valueOf(150));

        Listing listing = new Listing();
        listing.setPrice(BigDecimal.valueOf(100));

        listingMapper.updateListing(listing, request);

        assertThat(listing.getPrice()).isEqualTo(BigDecimal.valueOf(150));
    }
}
