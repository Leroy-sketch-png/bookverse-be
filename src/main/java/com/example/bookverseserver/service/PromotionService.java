package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Promotion.PromotionCreateRequest;
import com.example.bookverseserver.dto.request.Promotion.PromotionUpdateRequest;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.dto.response.Promotion.PromotionResponse;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.Product.Promotion;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.PromotionStatus;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.PromotionMapper;
import com.example.bookverseserver.repository.ListingRepository;
import com.example.bookverseserver.repository.PromotionRepository;
import com.example.bookverseserver.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Promotion Service - per Vision API_CONTRACTS.md §7.5 Promotions.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PromotionService {
    
    PromotionRepository promotionRepository;
    ListingRepository listingRepository;
    UserRepository userRepository;
    PromotionMapper promotionMapper;
    
    // ============ CRUD ============
    
    @Transactional
    public PromotionResponse createPromotion(Long sellerId, PromotionCreateRequest request) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        
        // Get listings and verify ownership
        Set<Listing> listings = new HashSet<>();
        for (Long listingId : request.getListingIds()) {
            Listing listing = listingRepository.findById(listingId)
                    .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND));
            
            if (!listing.getSeller().getId().equals(sellerId)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            listings.add(listing);
        }
        
        // Determine initial status
        PromotionStatus status = determineStatus(request.getStartDate(), request.getEndDate());
        
        Promotion promotion = Promotion.builder()
                .seller(seller)
                .name(request.getName())
                .discountPercentage(request.getDiscountPercentage())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(status)
                .appliedListings(listings)
                .build();
        
        Promotion saved = promotionRepository.save(promotion);
        log.info("Created promotion {} for seller {}", saved.getId(), sellerId);
        
        return promotionMapper.toResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public PagedResponse<PromotionResponse> getSellerPromotions(
            Long sellerId,
            PromotionStatus status,
            int page,
            int limit) {
        
        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        Page<Promotion> promotionsPage = promotionRepository.findBySellerId(sellerId, pageRequest);
        
        List<PromotionResponse> responses = promotionMapper.toResponseList(promotionsPage.getContent());
        
        return PagedResponse.of(
                responses,
                promotionsPage.getNumber(),
                promotionsPage.getSize(),
                promotionsPage.getTotalElements(),
                promotionsPage.getTotalPages()
        );
    }
    
    @Transactional(readOnly = true)
    public PromotionResponse getPromotion(Long sellerId, Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
        
        if (!promotion.getSeller().getId().equals(sellerId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        return promotionMapper.toResponse(promotion);
    }
    
    @Transactional
    public PromotionResponse updatePromotion(Long sellerId, Long promotionId, PromotionUpdateRequest request) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
        
        if (!promotion.getSeller().getId().equals(sellerId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        // Update fields if provided
        if (request.getName() != null) {
            promotion.setName(request.getName());
        }
        if (request.getDiscountPercentage() != null) {
            promotion.setDiscountPercentage(request.getDiscountPercentage());
        }
        if (request.getStartDate() != null) {
            promotion.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            promotion.setEndDate(request.getEndDate());
        }
        
        // Validate dates if both are set
        if (promotion.getEndDate().isBefore(promotion.getStartDate())) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        
        // Update listings if provided
        if (request.getListingIds() != null && !request.getListingIds().isEmpty()) {
            Set<Listing> listings = new HashSet<>();
            for (Long listingId : request.getListingIds()) {
                Listing listing = listingRepository.findById(listingId)
                        .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND));
                
                if (!listing.getSeller().getId().equals(sellerId)) {
                    throw new AppException(ErrorCode.UNAUTHORIZED);
                }
                listings.add(listing);
            }
            promotion.setAppliedListings(listings);
        }
        
        // Recalculate status
        promotion.setStatus(determineStatus(promotion.getStartDate(), promotion.getEndDate()));
        
        Promotion saved = promotionRepository.save(promotion);
        return promotionMapper.toResponse(saved);
    }
    
    @Transactional
    public void deletePromotion(Long sellerId, Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
        
        if (!promotion.getSeller().getId().equals(sellerId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        promotionRepository.delete(promotion);
        log.info("Deleted promotion {} for seller {}", promotionId, sellerId);
    }
    
    // ============ Actions ============
    
    @Transactional
    public PromotionResponse activatePromotion(Long sellerId, Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
        
        if (!promotion.getSeller().getId().equals(sellerId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        promotion.setStatus(PromotionStatus.ACTIVE);
        Promotion saved = promotionRepository.save(promotion);
        return promotionMapper.toResponse(saved);
    }
    
    @Transactional
    public PromotionResponse pausePromotion(Long sellerId, Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
        
        if (!promotion.getSeller().getId().equals(sellerId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        promotion.setStatus(PromotionStatus.PAUSED);
        Promotion saved = promotionRepository.save(promotion);
        return promotionMapper.toResponse(saved);
    }
    
    // ============ Helpers ============
    
    private PromotionStatus determineStatus(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startDate)) {
            return PromotionStatus.SCHEDULED;
        } else if (now.isAfter(endDate)) {
            return PromotionStatus.EXPIRED;
        } else {
            return PromotionStatus.ACTIVE;
        }
    }
}
