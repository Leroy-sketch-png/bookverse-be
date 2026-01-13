package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.ShippingAddress.ShippingAddressRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.ShippingAddress.ShippingAddressResponse;
import com.example.bookverseserver.service.ShippingAddressService;
import com.example.bookverseserver.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shipping-addresses")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Shipping Address", description = "Shipping address management APIs")
public class ShippingAddressController {

    ShippingAddressService shippingAddressService;
    SecurityUtils securityUtils;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create shipping address", description = "Create a new shipping address for the current user")
    public ApiResponse<ShippingAddressResponse> createShippingAddress(
            Authentication authentication,
            @Valid @RequestBody ShippingAddressRequest request
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        ShippingAddressResponse response = shippingAddressService.createShippingAddress(userId, request);
        return ApiResponse.<ShippingAddressResponse>builder()
                .code(1000)
                .message("Shipping address created successfully")
                .result(response)
                .build();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all shipping addresses", description = "Get all shipping addresses for the current user")
    public ApiResponse<List<ShippingAddressResponse>> getShippingAddresses(Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        List<ShippingAddressResponse> responses = shippingAddressService.getShippingAddressesByUserId(userId);
        return ApiResponse.<List<ShippingAddressResponse>>builder()
                .code(1000)
                .message("Shipping addresses retrieved successfully")
                .result(responses)
                .build();
    }

    @GetMapping("/{addressId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get shipping address by ID", description = "Get a specific shipping address by ID")
    public ApiResponse<ShippingAddressResponse> getShippingAddress(
            Authentication authentication,
            @PathVariable Long addressId
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        ShippingAddressResponse response = shippingAddressService.getShippingAddressById(userId, addressId);
        return ApiResponse.<ShippingAddressResponse>builder()
                .code(1000)
                .message("Shipping address retrieved successfully")
                .result(response)
                .build();
    }

    @GetMapping("/default")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get default shipping address", description = "Get the default shipping address for the current user")
    public ApiResponse<ShippingAddressResponse> getDefaultAddress(Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        ShippingAddressResponse response = shippingAddressService.getDefaultAddress(userId);
        return ApiResponse.<ShippingAddressResponse>builder()
                .code(1000)
                .message("Default shipping address retrieved successfully")
                .result(response)
                .build();
    }

    @PutMapping("/{addressId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update shipping address", description = "Update an existing shipping address")
    public ApiResponse<ShippingAddressResponse> updateShippingAddress(
            Authentication authentication,
            @PathVariable Long addressId,
            @Valid @RequestBody ShippingAddressRequest request
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        ShippingAddressResponse response = shippingAddressService.updateShippingAddress(userId, addressId, request);
        return ApiResponse.<ShippingAddressResponse>builder()
                .code(1000)
                .message("Shipping address updated successfully")
                .result(response)
                .build();
    }

    @PatchMapping("/{addressId}/default")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Set default address", description = "Set a shipping address as the default")
    public ApiResponse<ShippingAddressResponse> setDefaultAddress(
            Authentication authentication,
            @PathVariable Long addressId
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        ShippingAddressResponse response = shippingAddressService.setDefaultAddress(userId, addressId);
        return ApiResponse.<ShippingAddressResponse>builder()
                .code(1000)
                .message("Default address set successfully")
                .result(response)
                .build();
    }

    @DeleteMapping("/{addressId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete shipping address", description = "Delete a shipping address")
    public ApiResponse<Void> deleteShippingAddress(
            Authentication authentication,
            @PathVariable Long addressId
    ) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        shippingAddressService.deleteShippingAddress(userId, addressId);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Shipping address deleted successfully")
                .build();
    }
}

