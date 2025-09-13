package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Product.ListingRequest;
import com.example.bookverseserver.dto.request.Product.ListingUpdateRequest;
import com.example.bookverseserver.dto.response.Product.ListingResponse;
import com.example.bookverseserver.dto.response.Product.ListingUpdateResponse;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.ListingMapper;
import com.example.bookverseserver.repository.ListingRepository;
import com.example.bookverseserver.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ListingService {
    ListingRepository listingRepository;
    SecurityUtils securityUtils;
    ListingMapper listingMapper;

    @PreAuthorize("hasRole('PRO')")
    public ListingResponse createListing (ListingRequest request) {
        Listing listing = listingMapper.toListing(request);
        listingRepository.save(listing);
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


}
