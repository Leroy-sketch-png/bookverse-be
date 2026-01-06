package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Promotion.PromotionCreateRequest;
import com.example.bookverseserver.dto.request.Promotion.PromotionUpdateRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.dto.response.Promotion.PromotionResponse;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.PromotionStatus;
import com.example.bookverseserver.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Promotion Controller - per Vision API_CONTRACTS.md §7.5 Promotions.
 * 
 * Endpoints:
 * - POST   /api/seller/promotions         - Create promotion
 * - GET    /api/seller/promotions         - List seller promotions
 * - GET    /api/seller/promotions/{id}    - Get promotion details
 * - PATCH  /api/seller/promotions/{id}    - Update promotion
 * - DELETE /api/seller/promotions/{id}    - Delete promotion
 * - POST   /api/seller/promotions/{id}/activate - Activate promotion
 * - POST   /api/seller/promotions/{id}/pause    - Pause promotion
 */
@RestController
@RequestMapping("/api/seller/promotions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Seller Promotions", description = "Promotion management for sellers")
@PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
public class PromotionController {
    
    PromotionService promotionService;
    
    // ============ CRUD Endpoints ============
    
    @PostMapping
    @Operation(summary = "Create a new promotion")
    public ApiResponse<PromotionResponse> createPromotion(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PromotionCreateRequest request) {
        
        log.info("Creating promotion for seller {}", user.getId());
        PromotionResponse response = promotionService.createPromotion(user.getId(), request);
        
        return ApiResponse.<PromotionResponse>builder()
                .result(response)
                .build();
    }
    
    @GetMapping
    @Operation(summary = "Get seller's promotions")
    public ApiResponse<PagedResponse<PromotionResponse>> getPromotions(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) PromotionStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        PagedResponse<PromotionResponse> response = promotionService.getSellerPromotions(
                user.getId(), status, page, limit);
        
        return ApiResponse.<PagedResponse<PromotionResponse>>builder()
                .result(response)
                .build();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get promotion details")
    public ApiResponse<PromotionResponse> getPromotion(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        
        PromotionResponse response = promotionService.getPromotion(user.getId(), id);
        
        return ApiResponse.<PromotionResponse>builder()
                .result(response)
                .build();
    }
    
    @PatchMapping("/{id}")
    @Operation(summary = "Update promotion")
    public ApiResponse<PromotionResponse> updatePromotion(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody PromotionUpdateRequest request) {
        
        PromotionResponse response = promotionService.updatePromotion(user.getId(), id, request);
        
        return ApiResponse.<PromotionResponse>builder()
                .result(response)
                .build();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete promotion")
    public ApiResponse<Void> deletePromotion(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        
        promotionService.deletePromotion(user.getId(), id);
        
        return ApiResponse.<Void>builder()
                .message("Promotion deleted successfully")
                .build();
    }
    
    // ============ Action Endpoints ============
    
    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate a promotion")
    public ApiResponse<PromotionResponse> activatePromotion(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        
        PromotionResponse response = promotionService.activatePromotion(user.getId(), id);
        
        return ApiResponse.<PromotionResponse>builder()
                .result(response)
                .build();
    }
    
    @PostMapping("/{id}/pause")
    @Operation(summary = "Pause a promotion")
    public ApiResponse<PromotionResponse> pausePromotion(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        
        PromotionResponse response = promotionService.pausePromotion(user.getId(), id);
        
        return ApiResponse.<PromotionResponse>builder()
                .result(response)
                .build();
    }
}
