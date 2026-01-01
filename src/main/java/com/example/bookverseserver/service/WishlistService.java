package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.Book.BookResponse;
import com.example.bookverseserver.dto.response.Product.ListingSummaryResponse;
import com.example.bookverseserver.dto.response.Wishlist.WishlistCheckDto;
import com.example.bookverseserver.dto.response.Wishlist.WishlistItemDTO;
import com.example.bookverseserver.dto.response.Wishlist.WishlistResponse;
import com.example.bookverseserver.entity.Product.BookMeta;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.Product.Wishlist;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.BookMetaRepository; // Giả định
import com.example.bookverseserver.repository.ListingRepository;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public WishlistResponse getUserFavorites(Long userId, Pageable pageable) {
        Page<Wishlist> wishlistPage = wishlistRepository.findByUserId(userId, pageable);

        var items = wishlistPage.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());

        return WishlistResponse.builder()
                .favorites(items)
                .totalFavorites(wishlistPage.getTotalElements())
                .build();
    }

    /**
     * Add Listing to Wishlist (Idempotent)
     */
    @Transactional
    public WishlistItemDTO addToWishlist(Long userId, Long listingId) {
        // 1. Validate Listing
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND, "Listing not found with id: " + listingId));

        // 2. Check Exists (Idempotent)
        Optional<Wishlist> existing = wishlistRepository.findByUserIdAndListingId(userId, listingId);
        if (existing.isPresent()) {
            return mapToDTO(existing.get());
        }

        // 3. Create New
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .listing(listing)
                .priceAtAddition(listing.getPrice()) // Save snapshot of price
                .build();

        Wishlist saved = wishlistRepository.save(wishlist);

        // SỬA: Xóa dòng 'wishlistPage' bị lỗi đi, trả về item vừa lưu
        return mapToDTO(saved);
    }

    /**
     * Remove Listing from Wishlist
     */
    @Transactional
    public void removeFromWishlist(Long userId, Long listingId) {
        wishlistRepository.deleteByUserIdAndListingId(userId, listingId);
    }

    /**
     * Check if Listing is in Wishlist
     */
    @Transactional(readOnly = true)
    public WishlistCheckDto checkIfInWishlist(Long userId, Long listingId) {
        Optional<Wishlist> wishlist = wishlistRepository.findByUserIdAndListingId(userId, listingId);

        if (wishlist.isPresent()) {
            return WishlistCheckDto.builder()
                    .inWishlist(true)
                    .wishlistItemId(wishlist.get().getId())
                    .addedAt(wishlist.get().getAddedAt())
                    .build();
        }
        return WishlistCheckDto.builder()
                .inWishlist(false)
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