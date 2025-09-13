package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Product.ListingRequest;
import com.example.bookverseserver.dto.request.Product.ListingUpdateRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Product.ListingResponse;
import com.example.bookverseserver.dto.response.Product.ListingUpdateResponse;
import com.example.bookverseserver.service.ListingService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listing")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ListingController {
    ListingService listingService;

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
}
