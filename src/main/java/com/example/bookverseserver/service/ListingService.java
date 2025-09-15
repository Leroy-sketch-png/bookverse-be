package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Product.*;
import com.example.bookverseserver.dto.response.Product.ListingResponse;
import com.example.bookverseserver.dto.response.Product.ListingUpdateResponse;
import com.example.bookverseserver.entity.Product.BookMeta;
import com.example.bookverseserver.entity.Product.Likes;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.Product.ListingPhoto;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.ListingStatus;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.BookMetaMapper;
import com.example.bookverseserver.mapper.ListingMapper;
import com.example.bookverseserver.mapper.ListingPhotoMapper;
import com.example.bookverseserver.repository.*;
import com.example.bookverseserver.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ListingService {
    ListingRepository listingRepository;
    ListingPhotoRepository listingPhotoRepository;
    LikesRepository likesRepository;
    UserRepository userRepository;
    BookMetaRepository bookMetaRepository;
    BookMetaMapper bookMetaMapper;
    ListingMapper listingMapper;
    ListingPhotoMapper listingPhotoMapper;
    SecurityUtils securityUtils;

    @PreAuthorize("hasRole('PRO')")
    @Transactional
    public ListingResponse createListing(ListingCreationRequest request, Authentication authentication) {
        BookMeta bookMeta;

        if (request.getBookMetaId() != null) {
            // Dùng bookMeta có sẵn
            bookMeta = bookMetaRepository.findById(request.getBookMetaId())
                    .orElseThrow(() -> new AppException(ErrorCode.BOOK_META_NOT_FOUND));
        } else if (request.getBookMetaPayload() != null) {
            // Tạo bookMeta mới
            bookMeta = bookMetaMapper.toBookMeta(request.getBookMetaPayload());
            bookMeta = bookMetaRepository.save(bookMeta);
        } else {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Must provide either bookMetaId or bookMetaPayload");
        }

        // Tạo listing
        Listing listing = listingMapper.toListing(request.getListing());
        listing.setBookMeta(bookMeta);
        listing.setSeller(userRepository.getReferenceById(securityUtils.getCurrentUserId(authentication)));
        listing = listingRepository.save(listing);

        // Tạo photos
        if (request.getPhotos() != null) {
            for (ListingPhotoRequest p : request.getPhotos()) {
                ListingPhoto photo = listingPhotoMapper.toListingPhoto(p);
                photo.setListing(listing);
                listingPhotoRepository.save(photo);
            }
        }

        return listingMapper.toListingResponse(listing);
    }


    //@PreAuthorize("hasRole('PRO')")
    public ListingUpdateResponse updateListing (Long listingId, ListingUpdateRequest request, Authentication authentication) {
        log.info(">>> Entered updateListing with listingId={}", listingId);

        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new AppException(ErrorCode.AUTHOR_NOT_EXISTED));

        Long currentUserId = securityUtils.getCurrentUserId(authentication);


        log.info("SellerId={}, CurrentUserId={}", listing.getSeller().getId(), currentUserId);
        if(listing.getSeller().getId().equals(currentUserId)){
            listingMapper.updateListing(listing, request);
            listing =  listingRepository.save(listing);
            return listingMapper.toListingUpdateResponse(listing);
        } else {
            throw new AppException(ErrorCode.DO_NOT_HAVE_PERMISSION);
        }
    }

    public String hardDeleteListing (Long listingId, Authentication authentication) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.AUTHOR_NOT_EXISTED));

        Long currentUserId = securityUtils.getCurrentUserId(authentication);

        if(listing.getSeller().getId().equals(currentUserId)){
            listingRepository.delete(listing);
            return "Successfully deleted listing";
        } else {
            throw new AppException(ErrorCode.DO_NOT_HAVE_PERMISSION);
        }
    }

    public ListingUpdateResponse softDeleteListing(Long listingId, Authentication authentication) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_EXISTED));

        Long currentUserId = securityUtils.getCurrentUserId(authentication);

        if (!listing.getSeller().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.DO_NOT_HAVE_PERMISSION);
        }

        listing.setDeletedAt(LocalDateTime.now());
        listing.setDeletedBy(currentUserId);
        listing.setStatus(ListingStatus.REMOVED);
        listing.setVisibility(false);

        listing = listingRepository.save(listing);
        return listingMapper.toListingUpdateResponse(listing);
    }

    @Transactional
    public ListingResponse toggleListingLike(Long listingId, Authentication authentication) {
        Long currentUserId = securityUtils.getCurrentUserId(authentication);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_EXISTED));

        if (listing.getSeller().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.DO_NOT_HAVE_PERMISSION);
        }

        if (likesRepository.existsByUserIdAndListingId(currentUserId, listingId)) {
            // already liked → unlike
            likesRepository.deleteByUserIdAndListingId(currentUserId, listingId);
            listing.setLikes(listing.getLikes() - 1);
        } else {
            // not yet liked → like
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            Likes like = new Likes();
            like.setListing(listing);
            like.setUser(currentUser);

            likesRepository.save(like);
            listing.setLikes(listing.getLikes() + 1);
        }

        listingRepository.save(listing);
        return listingMapper.toListingResponse(listing);
    }

    public ListingResponse getListingById (Long listingId, Authentication authentication) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_EXISTED));
        if(!listing.getSeller().getId().equals(securityUtils.getCurrentUserId(authentication))){
            listing.setViews(listing.getViews() + 1);
            listingRepository.save(listing);
        }

        return listingMapper.toListingResponse(listing);
    }

    public List<ListingResponse> getAllListings() {
        List<Listing> listings = listingRepository.findAll();
        if(listings.isEmpty()){
            throw new AppException(ErrorCode.NO_LISTING_FOUND);
        }

        return listings.stream()
                .map(listingMapper::toListingResponse)
                .toList();
    }
}
