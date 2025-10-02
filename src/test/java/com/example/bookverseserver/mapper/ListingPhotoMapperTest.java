package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Product.ListingPhotoRequest;
import com.example.bookverseserver.dto.response.Product.ListingPhotoResponse;
import com.example.bookverseserver.entity.Product.ListingPhoto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ListingPhotoMapperTest {

    @Autowired
    private ListingPhotoMapper listingPhotoMapper;

    @Test
    void testToListingPhoto() {
        ListingPhotoRequest request = new ListingPhotoRequest();
        request.setUrl("http://example.com/image.jpg");
        request.setPosition(1);

        ListingPhoto listingPhoto = listingPhotoMapper.toListingPhoto(request);

        assertThat(listingPhoto).isNotNull();
        assertThat(listingPhoto.getUrl()).isEqualTo("http://example.com/image.jpg");
        assertThat(listingPhoto.getPosition()).isEqualTo(1);
    }

    @Test
    void testToListingPhotoResponse() {
        ListingPhoto listingPhoto = new ListingPhoto();
        listingPhoto.setId(1L);
        listingPhoto.setUrl("http://example.com/image.jpg");
        listingPhoto.setPosition(1);
        listingPhoto.setCreatedAt(LocalDateTime.now());

        ListingPhotoResponse response = listingPhotoMapper.toListingPhotoResponse(listingPhoto);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUrl()).isEqualTo("http://example.com/image.jpg");
        assertThat(response.getPosition()).isEqualTo(1);
        assertThat(response.getCreatedAt()).isNotNull();
    }
}
