package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.Book.BookResponse;
import com.example.bookverseserver.dto.response.Product.ListingSummaryResponse;
import com.example.bookverseserver.dto.response.Wishlist.WishlistCheckDto;
import com.example.bookverseserver.dto.response.Wishlist.WishlistCountDto;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // Default read-only, override for write methods
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public WishlistResponse getUserFavorites(Long userId, Pageable pageable) {
        // Use custom query with JOIN FETCH to prevent LazyInitializationException
        List<Wishlist> allWishlists = wishlistRepository.findByUserIdWithDetails(userId);
        
        // Manual pagination since we can't use Pageable with JOIN FETCH multiple bags
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allWishlists.size());
        
        List<WishlistItemDTO> items = allWishlists.subList(
                Math.min(start, allWishlists.size()), 
                Math.min(end, allWishlists.size())
            ).stream()
            .map(this::mapToDTO)
            .collect(java.util.stream.Collectors.toList());

        return WishlistResponse.builder()
                .items(items)
                .totalFavorites((long) allWishlists.size())
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

        // 2. Prevent sellers from wishlisting their own listing
        if (listing.getSeller().getId().equals(userId)) {
            throw new AppException(ErrorCode.CANNOT_WISHLIST_OWN_LISTING);
        }

        // 3. Check Exists (Idempotent)
        Optional<Wishlist> existing = wishlistRepository.findByUserIdAndListingId(userId, listingId);
        if (existing.isPresent()) {
            return mapToDTO(existing.get());
        }

        // 4. Create New
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

    /**
     * Get Count
     */
    @Transactional(readOnly = true)
    public WishlistCountDto getWishlistCount(Long userId) {
        return new WishlistCountDto(wishlistRepository.countByUserId(userId));
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