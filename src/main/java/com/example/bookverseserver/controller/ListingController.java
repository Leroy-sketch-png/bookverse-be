package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Product.ListingCreationRequest;
import com.example.bookverseserver.dto.request.Product.ListingDeleteRequest;
import com.example.bookverseserver.dto.request.Product.ListingRequest;
import com.example.bookverseserver.dto.request.Product.ListingUpdateRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Product.ListingResponse;
import com.example.bookverseserver.dto.response.Product.ListingUpdateResponse;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.service.ListingService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listing")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ListingController {
    ListingService listingService;

    @GetMapping("/all")
    public ApiResponse<List<ListingResponse>> getAllListings() {
        return ApiResponse.<List<ListingResponse>>builder()
                .result(listingService.getAllListings())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ListingResponse> getListingById(@PathVariable("id") Long listingId, Authentication authentication) {
        return ApiResponse.<ListingResponse>builder()
                .result(listingService.getListingById(listingId, authentication))
                .build();
    }

    @PostMapping("/create")
    public ApiResponse<ListingResponse> createListing(@RequestBody ListingCreationRequest request, Authentication authentication) {
        return ApiResponse.<ListingResponse>builder()
                .result(listingService.createListing(request, authentication))
                .build();
    }

    @PutMapping("/sell/{quantity}")
    public ApiResponse<ListingResponse> updateListingSoldCount(@RequestParam("id") Long id, @PathVariable("quantity") Integer quantity, Authentication authentication) {
        return ApiResponse.<ListingResponse>builder()
                .result(listingService.updateListingSoldCount(id, quantity, authentication))
                .build();
    }

    @PutMapping("/update")
    public ApiResponse<ListingUpdateResponse> update(
            @RequestParam("id") Long id,
            @RequestBody ListingUpdateRequest request,
            Authentication authentication
    ) {
        log.info(">>> Entered controller update with id={}, user={}", id, authentication != null ? authentication.getName() : "null");

        return ApiResponse.<ListingUpdateResponse>builder()
                .result(listingService.updateListing(id, request, authentication))
                .build();
    }

    @DeleteMapping("/delete")
    public ApiResponse<String> delete(@RequestParam("id") Long id, Authentication authentication) {
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

    @PutMapping("/toggle-like")
    public ApiResponse<ListingResponse> toggleListingLike(
            @RequestParam("id") Long id,
            Authentication authentication
    ) {
        return ApiResponse.<ListingResponse>builder()
                .result(listingService.toggleListingLike(id, authentication))
                .build();
    }
}
