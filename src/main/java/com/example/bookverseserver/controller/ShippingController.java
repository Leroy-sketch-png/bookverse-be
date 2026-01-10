package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.ShippingRateResponse;
import com.example.bookverseserver.service.ShippingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Shipping Controller
 * 
 * Public endpoints for shipping rate calculation and address lookup.
 * Integrates with GHN (Giao Hang Nhanh) sandbox API.
 */
@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ShippingController {

    ShippingService shippingService;

    /**
     * Calculate shipping fee for a destination
     */
    @GetMapping("/calculate")
    public ApiResponse<ShippingRateResponse> calculateShippingFee(
            @RequestParam int toDistrictId,
            @RequestParam String toWardCode,
            @RequestParam(defaultValue = "500") int weightInGrams,
            @RequestParam(defaultValue = "1") int quantity
    ) {
        ShippingRateResponse rate = shippingService.calculateShippingFee(
                toDistrictId, toWardCode, weightInGrams, quantity);
        
        return ApiResponse.<ShippingRateResponse>builder()
                .result(rate)
                .build();
    }

    /**
     * Get available shipping services for a destination
     */
    @GetMapping("/services")
    public ApiResponse<List<ShippingRateResponse>> getAvailableServices(
            @RequestParam int toDistrictId,
            @RequestParam String toWardCode
    ) {
        List<ShippingRateResponse> services = shippingService.getAvailableServices(toDistrictId, toWardCode);
        
        return ApiResponse.<List<ShippingRateResponse>>builder()
                .result(services)
                .build();
    }

    /**
     * Track a shipment by tracking number
     */
    @GetMapping("/track/{trackingNumber}")
    public ApiResponse<Map<String, Object>> trackOrder(@PathVariable String trackingNumber) {
        Map<String, Object> tracking = shippingService.trackOrder(trackingNumber);
        
        return ApiResponse.<Map<String, Object>>builder()
                .result(tracking)
                .build();
    }

    /**
     * Get list of provinces (for address picker)
     */
    @GetMapping("/provinces")
    public ApiResponse<List<Map<String, Object>>> getProvinces() {
        List<Map<String, Object>> provinces = shippingService.getProvinces();
        
        return ApiResponse.<List<Map<String, Object>>>builder()
                .result(provinces)
                .build();
    }

    /**
     * Get districts by province (for address picker)
     */
    @GetMapping("/provinces/{provinceId}/districts")
    public ApiResponse<List<Map<String, Object>>> getDistricts(@PathVariable int provinceId) {
        List<Map<String, Object>> districts = shippingService.getDistricts(provinceId);
        
        return ApiResponse.<List<Map<String, Object>>>builder()
                .result(districts)
                .build();
    }

    /**
     * Get wards by district (for address picker)
     */
    @GetMapping("/districts/{districtId}/wards")
    public ApiResponse<List<Map<String, Object>>> getWards(@PathVariable int districtId) {
        List<Map<String, Object>> wards = shippingService.getWards(districtId);
        
        return ApiResponse.<List<Map<String, Object>>>builder()
                .result(wards)
                .build();
    }
}
