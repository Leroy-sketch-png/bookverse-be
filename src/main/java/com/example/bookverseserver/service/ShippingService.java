package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.ShippingRateResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * GHN (Giao Hang Nhanh) Shipping Service
 * 
 * Integrates with GHN's sandbox API for shipping rate calculation and order tracking.
 * Vietnam's most popular shipping carrier with a developer-friendly API.
 * 
 * Sandbox URL: https://dev-online-gateway.ghn.vn
 * Production URL: https://online-gateway.ghn.vn
 * 
 * API Docs: https://api.ghn.vn/home/docs/detail
 * 
 * @see <a href="https://ghn.vn">Giao Hang Nhanh</a>
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class ShippingService {

    final RestTemplate restTemplate = new RestTemplate();
    final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ghn.api.token:PLACEHOLDER_REPLACE_ME}")
    String token;

    @Value("${ghn.shop.id:PLACEHOLDER_REPLACE_ME}")
    String shopId;

    @Value("${ghn.api.url:https://dev-online-gateway.ghn.vn}")
    String apiUrl;

    @Value("${ghn.enabled:false}")
    boolean enabled;

    // Default warehouse location (configured per seller in production)
    private static final int DEFAULT_FROM_DISTRICT = 1442; // District 1, Ho Chi Minh
    private static final String DEFAULT_FROM_WARD = "20101"; // Default ward

    /**
     * Calculate shipping fee based on destination and weight
     * 
     * POST /shiip/public-api/v2/shipping-order/fee
     */
    public ShippingRateResponse calculateShippingFee(
            int toDistrictId,
            String toWardCode,
            int weightInGrams,
            int quantity
    ) {
        // Use fallback rates if not configured or if API fails
        if (!enabled || token.contains("PLACEHOLDER")) {
            return getFallbackShippingRate(toDistrictId, weightInGrams);
        }

        try {
            String url = apiUrl + "/shiip/public-api/v2/shipping-order/fee";

            HttpHeaders headers = createHeaders();
            
            Map<String, Object> body = new HashMap<>();
            body.put("from_district_id", DEFAULT_FROM_DISTRICT);
            body.put("to_district_id", toDistrictId);
            body.put("to_ward_code", toWardCode);
            body.put("weight", weightInGrams);
            body.put("service_type_id", 2); // Standard delivery
            body.put("insurance_value", 0);
            body.put("items", List.of(
                    Map.of(
                            "name", "Books",
                            "quantity", quantity,
                            "weight", weightInGrams / quantity
                    )
            ));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                if (json.has("data")) {
                    JsonNode data = json.get("data");
                    return ShippingRateResponse.builder()
                            .fee(new BigDecimal(data.get("total").asText()))
                            .serviceFee(new BigDecimal(data.has("service_fee") ? data.get("service_fee").asText() : "0"))
                            .insuranceFee(new BigDecimal(data.has("insurance_fee") ? data.get("insurance_fee").asText() : "0"))
                            .estimatedDeliveryDate(calculateEstimatedDelivery(data))
                            .serviceType("GHN Standard")
                            .carrierName("Giao Hang Nhanh")
                            .build();
                }
            }

        } catch (Exception e) {
            log.error("[GHN] Error calculating shipping fee: {}", e.getMessage());
        }

        return getFallbackShippingRate(toDistrictId, weightInGrams);
    }

    /**
     * Get available shipping services
     * 
     * POST /shiip/public-api/v2/shipping-order/available-services
     */
    public List<ShippingRateResponse> getAvailableServices(int toDistrictId, String toWardCode) {
        List<ShippingRateResponse> services = new ArrayList<>();

        if (!enabled || token.contains("PLACEHOLDER")) {
            // Return mock services
            services.add(ShippingRateResponse.builder()
                    .serviceType("Standard")
                    .fee(new BigDecimal("25000"))
                    .estimatedDeliveryDate(LocalDate.now().plusDays(3).format(DateTimeFormatter.ISO_DATE))
                    .carrierName("GHN (Sandbox)")
                    .build());
            services.add(ShippingRateResponse.builder()
                    .serviceType("Express")
                    .fee(new BigDecimal("45000"))
                    .estimatedDeliveryDate(LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE))
                    .carrierName("GHN (Sandbox)")
                    .build());
            return services;
        }

        try {
            String url = apiUrl + "/shiip/public-api/v2/shipping-order/available-services";

            HttpHeaders headers = createHeaders();
            
            Map<String, Object> body = new HashMap<>();
            body.put("shop_id", Integer.parseInt(shopId));
            body.put("from_district", DEFAULT_FROM_DISTRICT);
            body.put("to_district", toDistrictId);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                if (json.has("data") && json.get("data").isArray()) {
                    for (JsonNode service : json.get("data")) {
                        services.add(ShippingRateResponse.builder()
                                .serviceType(service.get("short_name").asText())
                                .carrierName("Giao Hang Nhanh")
                                .build());
                    }
                }
            }

        } catch (Exception e) {
            log.error("[GHN] Error getting available services: {}", e.getMessage());
        }

        return services;
    }

    /**
     * Create shipping order and get tracking number
     * 
     * POST /shiip/public-api/v2/shipping-order/create
     */
    public Map<String, String> createShippingOrder(
            String toName,
            String toPhone,
            String toAddress,
            int toDistrictId,
            String toWardCode,
            int weightInGrams,
            int codAmount,
            String orderNote
    ) {
        Map<String, String> result = new HashMap<>();

        if (!enabled || token.contains("PLACEHOLDER")) {
            // Generate mock tracking number
            String mockTracking = "BV" + System.currentTimeMillis();
            result.put("trackingNumber", mockTracking);
            result.put("orderId", mockTracking);
            result.put("expectedDelivery", LocalDate.now().plusDays(3).format(DateTimeFormatter.ISO_DATE));
            log.info("[GHN MOCK] Created shipping order with tracking: {}", mockTracking);
            return result;
        }

        try {
            String url = apiUrl + "/shiip/public-api/v2/shipping-order/create";

            HttpHeaders headers = createHeaders();
            
            Map<String, Object> body = new HashMap<>();
            body.put("to_name", toName);
            body.put("to_phone", toPhone);
            body.put("to_address", toAddress);
            body.put("to_district_id", toDistrictId);
            body.put("to_ward_code", toWardCode);
            body.put("weight", weightInGrams);
            body.put("service_type_id", 2);
            body.put("payment_type_id", codAmount > 0 ? 2 : 1); // 1 = seller pays, 2 = buyer pays (COD)
            body.put("cod_amount", codAmount);
            body.put("required_note", "CHOXEMHANGKHONGTHU"); // Don't allow try-before-buy
            body.put("note", orderNote);
            body.put("items", List.of(Map.of(
                    "name", "Sách / Books",
                    "quantity", 1,
                    "weight", weightInGrams
            )));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                if (json.has("data")) {
                    JsonNode data = json.get("data");
                    result.put("trackingNumber", data.get("order_code").asText());
                    result.put("orderId", data.get("order_code").asText());
                    result.put("expectedDelivery", data.has("expected_delivery_time") 
                            ? data.get("expected_delivery_time").asText() : "");
                    log.info("[GHN] Created shipping order: {}", result.get("trackingNumber"));
                    return result;
                }
            }

        } catch (Exception e) {
            log.error("[GHN] Error creating shipping order: {}", e.getMessage());
        }

        // Fallback to mock
        String mockTracking = "BV" + System.currentTimeMillis();
        result.put("trackingNumber", mockTracking);
        result.put("orderId", mockTracking);
        result.put("expectedDelivery", LocalDate.now().plusDays(3).format(DateTimeFormatter.ISO_DATE));
        return result;
    }

    /**
     * Track shipping order status
     * 
     * POST /shiip/public-api/v2/shipping-order/detail
     */
    public Map<String, Object> trackOrder(String trackingNumber) {
        Map<String, Object> result = new HashMap<>();

        if (!enabled || token.contains("PLACEHOLDER") || trackingNumber.startsWith("BV")) {
            // Return mock tracking info
            result.put("status", "delivering");
            result.put("statusDescription", "Đang giao hàng");
            result.put("lastUpdate", java.time.LocalDateTime.now().toString());
            result.put("estimatedDelivery", LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE));
            return result;
        }

        try {
            String url = apiUrl + "/shiip/public-api/v2/shipping-order/detail";

            HttpHeaders headers = createHeaders();
            
            Map<String, Object> body = new HashMap<>();
            body.put("order_code", trackingNumber);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                if (json.has("data")) {
                    JsonNode data = json.get("data");
                    result.put("status", data.get("status").asText());
                    result.put("statusDescription", getStatusDescription(data.get("status").asText()));
                    result.put("lastUpdate", data.has("updated_date") ? data.get("updated_date").asText() : "");
                    return result;
                }
            }

        } catch (Exception e) {
            log.error("[GHN] Error tracking order {}: {}", trackingNumber, e.getMessage());
        }

        result.put("status", "unknown");
        result.put("statusDescription", "Không thể tra cứu");
        return result;
    }

    /**
     * Get provinces list
     * 
     * GET /shiip/public-api/master-data/province
     */
    public List<Map<String, Object>> getProvinces() {
        List<Map<String, Object>> provinces = new ArrayList<>();

        try {
            String url = apiUrl + "/shiip/public-api/master-data/province";
            HttpHeaders headers = createHeaders();
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                if (json.has("data") && json.get("data").isArray()) {
                    for (JsonNode province : json.get("data")) {
                        Map<String, Object> p = new HashMap<>();
                        p.put("id", province.get("ProvinceID").asInt());
                        p.put("name", province.get("ProvinceName").asText());
                        provinces.add(p);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[GHN] Error getting provinces: {}", e.getMessage());
        }

        return provinces;
    }

    /**
     * Get districts by province
     * 
     * POST /shiip/public-api/master-data/district
     */
    public List<Map<String, Object>> getDistricts(int provinceId) {
        List<Map<String, Object>> districts = new ArrayList<>();

        try {
            String url = apiUrl + "/shiip/public-api/master-data/district";
            HttpHeaders headers = createHeaders();
            
            Map<String, Object> body = Map.of("province_id", provinceId);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                if (json.has("data") && json.get("data").isArray()) {
                    for (JsonNode district : json.get("data")) {
                        Map<String, Object> d = new HashMap<>();
                        d.put("id", district.get("DistrictID").asInt());
                        d.put("name", district.get("DistrictName").asText());
                        districts.add(d);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[GHN] Error getting districts: {}", e.getMessage());
        }

        return districts;
    }

    /**
     * Get wards by district
     * 
     * POST /shiip/public-api/master-data/ward
     */
    public List<Map<String, Object>> getWards(int districtId) {
        List<Map<String, Object>> wards = new ArrayList<>();

        try {
            String url = apiUrl + "/shiip/public-api/master-data/ward";
            HttpHeaders headers = createHeaders();
            
            Map<String, Object> body = Map.of("district_id", districtId);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                if (json.has("data") && json.get("data").isArray()) {
                    for (JsonNode ward : json.get("data")) {
                        Map<String, Object> w = new HashMap<>();
                        w.put("code", ward.get("WardCode").asText());
                        w.put("name", ward.get("WardName").asText());
                        wards.add(w);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[GHN] Error getting wards: {}", e.getMessage());
        }

        return wards;
    }

    // ========== Helper methods ==========

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", token);
        headers.set("ShopId", shopId);
        return headers;
    }

    private ShippingRateResponse getFallbackShippingRate(int toDistrictId, int weightInGrams) {
        // Calculate fallback based on distance/weight
        BigDecimal baseFee = new BigDecimal("25000"); // 25,000 VND base
        BigDecimal weightFee = new BigDecimal(weightInGrams / 500).multiply(new BigDecimal("5000")); // 5k per 500g
        BigDecimal totalFee = baseFee.add(weightFee);

        return ShippingRateResponse.builder()
                .fee(totalFee)
                .serviceFee(baseFee)
                .insuranceFee(BigDecimal.ZERO)
                .estimatedDeliveryDate(LocalDate.now().plusDays(3).format(DateTimeFormatter.ISO_DATE))
                .serviceType("Standard (Fallback)")
                .carrierName("Bookverse Delivery")
                .build();
    }

    private String calculateEstimatedDelivery(JsonNode data) {
        // GHN returns expected delivery time in various formats
        if (data.has("expected_delivery_time")) {
            return data.get("expected_delivery_time").asText();
        }
        // Default to 3 days from now
        return LocalDate.now().plusDays(3).format(DateTimeFormatter.ISO_DATE);
    }

    private String getStatusDescription(String status) {
        return switch (status) {
            case "ready_to_pick" -> "Chờ lấy hàng";
            case "picking" -> "Đang lấy hàng";
            case "picked" -> "Đã lấy hàng";
            case "storing" -> "Đang lưu kho";
            case "transporting" -> "Đang vận chuyển";
            case "sorting" -> "Đang phân loại";
            case "delivering" -> "Đang giao hàng";
            case "delivered" -> "Đã giao hàng";
            case "delivery_fail" -> "Giao hàng thất bại";
            case "return" -> "Đang trả hàng";
            case "returned" -> "Đã trả hàng";
            case "cancel" -> "Đã hủy";
            default -> "Không xác định";
        };
    }
}
