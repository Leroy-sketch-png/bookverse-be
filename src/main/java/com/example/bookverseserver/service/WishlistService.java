package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.Book.BookResponse;
import com.example.bookverseserver.dto.response.Product.ListingSummaryResponse;
import com.example.bookverseserver.dto.response.Wishlist.WishlistItemDTO;
import com.example.bookverseserver.dto.response.Wishlist.WishlistResponse;
import com.example.bookverseserver.entity.Product.BookMeta;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.Product.Wishlist;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.repository.BookMetaRepository; // Giả định
import com.example.bookverseserver.repository.WishlistRepository;
import com.example.bookverseserver.repository.UserRepository; // Giả định
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final BookMetaRepository bookMetaRepository;
    private final UserRepository userRepository;

    public WishlistResponse getUserFavorites(Long userId, Pageable pageable) {
        Page<Wishlist> wishlistPage = wishlistRepository.findByUserId(userId, pageable);

        var items = wishlistPage.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());

        return WishlistResponse.builder()
                .favorites(items)
                .totalFavorites(wishlistPage.getTotalElements())
                .build();
    }

    // --- Helper Mapper ---
    private WishlistItemDTO mapToDTO(Wishlist wishlist) {
        Listing listing = wishlist.getListing();

        BigDecimal currentPrice = listing.getPrice();
        BigDecimal addedPrice = wishlist.getPriceAtAddition();

        // Tính toán Price Drop
        BigDecimal priceDrop = addedPrice.subtract(currentPrice);
        if (priceDrop.compareTo(BigDecimal.ZERO) < 0) {
            priceDrop = BigDecimal.ZERO;
        }

        double dropPercentage = 0;
        if (addedPrice.compareTo(BigDecimal.ZERO) > 0) {
            dropPercentage = priceDrop.divide(addedPrice, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }

        ListingSummaryResponse listingSummary = mapToSummary(listing);

        return WishlistItemDTO.builder()
                .id(wishlist.getId())
                .listing(listingSummary)
                .addedAt(wishlist.getAddedAt())
                .priceAtAddition(addedPrice)
                .currentPrice(currentPrice)
                .priceDrop(priceDrop)
                .priceDropPercentage(dropPercentage)
                .inStock(listing.getQuantity() > 0)
                .build();
    }

    public ListingSummaryResponse mapToSummary(Listing listing) {
        // 1. Xử lý logic hiển thị tên sách
        String displayTitle = (listing.getTitleOverride() != null && !listing.getTitleOverride().isEmpty())
                ? listing.getTitleOverride()
                : listing.getBookMeta().getTitle();

        // 2. Lấy ảnh đầu tiên làm ảnh đại diện
        String mainPhotoUrl = null;
        if (listing.getPhotos() != null && !listing.getPhotos().isEmpty()) {
            mainPhotoUrl = listing.getPhotos().get(0).getUrl(); // Giả sử ListingPhoto có field url
        }

        return ListingSummaryResponse.builder()
                .id(listing.getId())
                .displayTitle(displayTitle)
                .price(listing.getPrice())
                .currency(listing.getCurrency())
                .mainPhotoUrl(mainPhotoUrl)
                .condition(listing.getCondition())
                .sellerName(listing.getSeller().getUserProfile().getFullName()) // Giả sử User có fullName
                .sellerId(listing.getSeller().getId())
                .status(listing.getStatus())
                .inStock(listing.getQuantity() > 0)
                .likes(listing.getLikes())
                .soldCount(listing.getSoldCount())
                .build();
    }
}