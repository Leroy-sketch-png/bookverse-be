package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Product.ListingCreationRequest;
import com.example.bookverseserver.dto.request.Product.ListingDeleteRequest;
import com.example.bookverseserver.dto.request.Product.ListingUpdateRequest;
import com.example.bookverseserver.dto.request.Product.SimpleListingCreationRequest;
import com.example.bookverseserver.dto.request.Product.UpdateStockRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.dto.response.Product.ListingDetailResponse;
import com.example.bookverseserver.dto.response.Product.ListingResponse;
import com.example.bookverseserver.dto.response.Product.ListingUpdateResponse;
import com.example.bookverseserver.dto.response.Product.StockUpdateResponse;
import com.example.bookverseserver.enums.ListingStatus;
import com.example.bookverseserver.service.ListingService;
import com.example.bookverseserver.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ListingController {
        ListingService listingService;
        SecurityUtils securityUtils;

        // ============ List Listings with Filters ============

        /**
         * Get paginated listings with optional filters and full-text search.
         * 
         * Query Parameters:
         * - q: full-text search query (searches book title, author names, listing description)
         * - sellerId: filter by seller
         * - bookId: filter by book
         * - categoryId: filter by category
         * - authorId: filter by author
         * - status: filter by status (ACTIVE, SOLD_OUT, DRAFT, etc.)
         * - sortBy: createdAt, price, viewCount, soldCount
         * - sortOrder: asc, desc
         * - page: page number (0-indexed)
         * - size: page size (default 20)
         */
        @GetMapping
        public ResponseEntity<ApiResponse<PagedResponse<ListingResponse>>> getListings(
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) Long sellerId,
                        @RequestParam(required = false) Long bookId,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) Long authorId,
                        @RequestParam(required = false) ListingStatus status,
                        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                        @RequestParam(required = false, defaultValue = "desc") String sortOrder,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "20") int size) {
                // Convert 1-indexed (API) to 0-indexed (Spring)
                int pageIndex = Math.max(0, page - 1);
                PagedResponse<ListingResponse> result = listingService.getListingsFiltered(
                                q, sellerId, bookId, categoryId, authorId, status, sortBy, sortOrder, pageIndex, size);

                return ResponseEntity.ok(ApiResponse.<PagedResponse<ListingResponse>>builder()
                                .result(result)
                                .build());
        }

        /**
         * Get listings by category slug.
         * Endpoint: GET /api/listings/category/{categorySlug}
         * Example: GET /api/listings/category/technology?page=0&size=20
         */
        @GetMapping("/category/{categorySlug}")
        public ResponseEntity<ApiResponse<PagedResponse<ListingResponse>>> getListingsByCategory(
                        @PathVariable String categorySlug,
                        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                        @RequestParam(required = false, defaultValue = "desc") String sortOrder,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "20") int size) {
                // Convert 1-indexed (API) to 0-indexed (Spring)
                int pageIndex = Math.max(0, page - 1);
                PagedResponse<ListingResponse> result = listingService.getListingsByCategory(
                                categorySlug, sortBy, sortOrder, pageIndex, size);

                return ResponseEntity.ok(ApiResponse.<PagedResponse<ListingResponse>>builder()
                                .result(result)
                                .build());
        }

        /**
         * Get listings by seller ID (public browsing).
         * Endpoint: GET /api/listings/seller/{sellerId}
         * Example: GET /api/listings/seller/2?page=0&size=20
         */
        @GetMapping("/seller/{sellerId}")
        public ResponseEntity<ApiResponse<PagedResponse<ListingResponse>>> getListingsBySeller(
                        @PathVariable Long sellerId,
                        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                        @RequestParam(required = false, defaultValue = "desc") String sortOrder,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "20") int size) {
                // Convert 1-indexed (API) to 0-indexed (Spring)
                int pageIndex = Math.max(0, page - 1);
                PagedResponse<ListingResponse> result = listingService.getListingsFiltered(
                                null, sellerId, null, null, null, ListingStatus.ACTIVE, sortBy, sortOrder, pageIndex, size);

                return ResponseEntity.ok(ApiResponse.<PagedResponse<ListingResponse>>builder()
                                .result(result)
                                .build());
        }

        /**
         * Legacy endpoint for listing all listings (no pagination).
         * Kept for backwards compatibility.
         */
        @GetMapping("/all")
        public ApiResponse<List<ListingResponse>> getAllListings() {
                return ApiResponse.<List<ListingResponse>>builder()
                                .result(listingService.getAllListings())
                                .build();
        }

        // ============ Get Listing Details ============

        /**
         * Get listing details by ID.
         * - Increments view count (except for seller's own views)
         * - Includes related listings (same book, different sellers)
         */
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<ListingDetailResponse>> getListingById(
                        @PathVariable("id") Long listingId,
                        Authentication authentication) {
                ListingDetailResponse detail = listingService.getListingDetail(listingId, authentication);

                return ResponseEntity.ok(ApiResponse.<ListingDetailResponse>builder()
                                .result(detail)
                                .build());
        }

        /**
         * Legacy endpoint - returns simple response.
         * Use GET /{id} for full details with related listings.
         */
        @GetMapping("/{id}/simple")
        public ApiResponse<ListingResponse> getListingByIdSimple(
                        @PathVariable("id") Long listingId,
                        Authentication authentication) {
                return ApiResponse.<ListingResponse>builder()
                                .result(listingService.getListingById(listingId, authentication))
                                .build();
        }

        // ============ Create Listing ============

        @PreAuthorize("hasRole('SELLER')")
        @PostMapping
        public ResponseEntity<ApiResponse<ListingResponse>> createListing(
                        @RequestBody ListingCreationRequest request,
                        Authentication authentication) {
                ListingResponse created = listingService.createListing(request, authentication);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<ListingResponse>builder()
                                                .result(created)
                                                .message("Listing created successfully")
                                                .build());
        }

        /**
         * Legacy create endpoint.
         */
        @PreAuthorize("hasRole('SELLER')")
        @PostMapping("/create")
        public ApiResponse<ListingResponse> createListingLegacy(
                        @RequestBody ListingCreationRequest request,
                        Authentication authentication) {
                return ApiResponse.<ListingResponse>builder()
                                .result(listingService.createListing(request, authentication))
                                .build();
        }

        /**
         * Simple create endpoint with multipart form data.
         * Accepts flat form fields + image files directly.
         * Creates book metadata if not found by ISBN.
         * 
         * This is the primary endpoint used by the frontend seller dashboard.
         */
        @PreAuthorize("hasRole('SELLER')")
        @PostMapping(value = "/simple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponse<ListingResponse>> createSimpleListing(
                        @Valid @ModelAttribute SimpleListingCreationRequest request,
                        @RequestParam(value = "images", required = false) List<MultipartFile> images,
                        Authentication authentication) {
                
                log.info("Creating simple listing: title={}, category={}, images={}", 
                        request.getTitle(), request.getCategory(), 
                        images != null ? images.size() : 0);

                ListingResponse created = listingService.createSimpleListing(request, images, authentication);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<ListingResponse>builder()
                                                .result(created)
                                                .message("Listing created successfully")
                                                .build());
        }

        // ============ Update Listing ============

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<ListingUpdateResponse>> updateListing(
                        @PathVariable("id") Long id,
                        @RequestBody ListingUpdateRequest request,
                        Authentication authentication) {
                log.info(">>> Entered controller update with id={}, user={}", id,
                                authentication != null ? authentication.getName() : "null");

                ListingUpdateResponse updated = listingService.updateListing(id, request, authentication);

                return ResponseEntity.ok(ApiResponse.<ListingUpdateResponse>builder()
                                .result(updated)
                                .build());
        }

        /**
         * Legacy update endpoint.
         */
        @PutMapping("/update")
        public ApiResponse<ListingUpdateResponse> updateListingLegacy(
                        @RequestParam("id") Long id,
                        @RequestBody ListingUpdateRequest request,
                        Authentication authentication) {
                log.info(">>> Entered controller update with id={}, user={}", id,
                                authentication != null ? authentication.getName() : "null");

                return ApiResponse.<ListingUpdateResponse>builder()
                                .result(listingService.updateListing(id, request, authentication))
                                .build();
        }

        // ============ Stock Management ============

        /**
         * Update listing stock.
         * Supports SET (exact value), ADD (increment), and SUBTRACT (decrement)
         * operations.
         * Status automatically updates to SOLD_OUT when quantity becomes 0.
         */
        @PatchMapping("/{id}/stock")
        public ResponseEntity<ApiResponse<StockUpdateResponse>> updateStock(
                        @PathVariable("id") Long id,
                        @Valid @RequestBody UpdateStockRequest request,
                        Authentication authentication) {
                Long userId = securityUtils.getCurrentUserId(authentication);
                StockUpdateResponse result = listingService.updateStock(id, userId, request);

                return ResponseEntity.ok(ApiResponse.<StockUpdateResponse>builder()
                                .result(result)
                                .message("Stock updated successfully")
                                .build());
        }

        /**
         * Legacy update sold count endpoint.
         */
        @PutMapping("/sell/{quantity}")
        public ApiResponse<ListingResponse> updateListingSoldCount(
                        @RequestParam("id") Long id,
                        @PathVariable("quantity") Integer quantity,
                        Authentication authentication) {
                return ApiResponse.<ListingResponse>builder()
                                .result(listingService.updateListingSoldCount(id, quantity, authentication))
                                .build();
        }

        // ============ Delete Listing ============

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<String>> deleteListing(
                        @PathVariable("id") Long id,
                        Authentication authentication) {
                String result = listingService.hardDeleteListing(id, authentication);

                return ResponseEntity.ok(ApiResponse.<String>builder()
                                .result(result)
                                .build());
        }

        /**
         * Legacy delete endpoint.
         */
        @DeleteMapping("/delete")
        public ApiResponse<String> deleteListingLegacy(
                        @RequestParam("id") Long id,
                        Authentication authentication) {
                return ApiResponse.<String>builder()
                                .result(listingService.hardDeleteListing(id, authentication))
                                .build();
        }

        @PutMapping("/soft-delete")
        public ApiResponse<ListingUpdateResponse> softDelete(
                        @RequestParam("id") Long id,
                        @RequestBody ListingDeleteRequest request,
                        Authentication authentication) {
                return ApiResponse.<ListingUpdateResponse>builder()
                                .result(listingService.softDeleteListing(id, authentication))
                                .build();
        }

        // ============ Like Toggle ============

        @PutMapping("/{id}/toggle-like")
        public ResponseEntity<ApiResponse<ListingResponse>> toggleListingLike(
                        @PathVariable("id") Long id,
                        Authentication authentication) {
                return ResponseEntity.ok(ApiResponse.<ListingResponse>builder()
                                .result(listingService.toggleListingLike(id, authentication))
                                .build());
        }

        /**
         * Legacy like toggle endpoint.
         */
        @PutMapping("/toggle-like")
        public ApiResponse<ListingResponse> toggleListingLikeLegacy(
                        @RequestParam("id") Long id,
                        Authentication authentication) {
                return ApiResponse.<ListingResponse>builder()
                                .result(listingService.toggleListingLike(id, authentication))
                                .build();
        }
}
