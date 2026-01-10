package com.example.bookverseserver.service;

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

import java.util.HashMap;
import java.util.Map;

/**
 * TextBee SMS Service
 * 
 * Integrates with TextBee API for sending SMS messages.
 * Used for: OTP verification, order notifications, payout alerts.
 * 
 * TextBee is a cost-effective SMS gateway supporting Vietnam phone numbers.
 * API Docs: https://textbee.dev/docs
 * 
 * @see <a href="https://textbee.dev">TextBee</a>
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class SmsService {

    final RestTemplate restTemplate = new RestTemplate();
    final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${textbee.api.key:PLACEHOLDER_REPLACE_ME}")
    String apiKey;

    @Value("${textbee.device.id:PLACEHOLDER_REPLACE_ME}")
    String deviceId;

    @Value("${textbee.api.url:https://api.textbee.dev/api/v1}")
    String apiUrl;

    @Value("${textbee.enabled:false}")
    boolean enabled;

    /**
     * Send OTP verification code
     */
    public boolean sendOtp(String phoneNumber, String otpCode) {
        String message = String.format(
                "[Bookverse] Mã xác thực của bạn là: %s. Mã có hiệu lực trong 10 phút.",
                otpCode
        );
        return sendSms(phoneNumber, message);
    }

    /**
     * Send order confirmation SMS
     */
    public boolean sendOrderConfirmation(String phoneNumber, String orderNumber, String totalAmount) {
        String message = String.format(
                "[Bookverse] Đơn hàng #%s đã được đặt thành công! Tổng tiền: %s VND. Cảm ơn bạn!",
                orderNumber, totalAmount
        );
        return sendSms(phoneNumber, message);
    }

    /**
     * Send order shipped notification
     */
    public boolean sendOrderShipped(String phoneNumber, String orderNumber, String trackingNumber) {
        String message = String.format(
                "[Bookverse] Đơn hàng #%s đã được giao cho đơn vị vận chuyển. Mã vận đơn: %s",
                orderNumber, trackingNumber
        );
        return sendSms(phoneNumber, message);
    }

    /**
     * Send order delivered notification
     */
    public boolean sendOrderDelivered(String phoneNumber, String orderNumber) {
        String message = String.format(
                "[Bookverse] Đơn hàng #%s đã được giao thành công. Cảm ơn bạn đã mua sắm!",
                orderNumber
        );
        return sendSms(phoneNumber, message);
    }

    /**
     * Send payout notification to seller
     */
    public boolean sendPayoutNotification(String phoneNumber, String amount, String status) {
        String message = String.format(
                "[Bookverse Seller] Yêu cầu rút tiền %s VND đã được %s.",
                amount, "COMPLETED".equals(status) ? "hoàn thành" : "xử lý"
        );
        return sendSms(phoneNumber, message);
    }

    /**
     * Core SMS sending method using TextBee API
     * 
     * POST /gateway/devices/{deviceId}/sendSMS
     * Body: { "recipients": ["+84..."], "message": "..." }
     */
    public boolean sendSms(String phoneNumber, String message) {
        if (!enabled) {
            log.info("[SMS DISABLED] Would send to {}: {}", phoneNumber, message);
            return true; // Simulate success when disabled
        }

        if (apiKey.contains("PLACEHOLDER") || deviceId.contains("PLACEHOLDER")) {
            log.warn("[SMS] TextBee not configured. Set textbee.api.key and textbee.device.id in properties.");
            log.info("[SMS MOCK] To: {}, Message: {}", phoneNumber, message);
            return true; // Don't fail, just log
        }

        try {
            String url = String.format("%s/gateway/devices/%s/sendSMS", apiUrl, deviceId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("recipients", new String[]{normalizePhoneNumber(phoneNumber)});
            body.put("message", message);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[SMS] Successfully sent to {}", phoneNumber);
                return true;
            } else {
                log.error("[SMS] Failed to send to {}. Status: {}", phoneNumber, response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("[SMS] Error sending to {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }

    /**
     * Normalize phone number to international format
     * Vietnam: 0xxx -> +84xxx
     */
    private String normalizePhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) return phone;
        
        phone = phone.replaceAll("[^0-9+]", "");
        
        if (phone.startsWith("0")) {
            return "+84" + phone.substring(1);
        } else if (!phone.startsWith("+")) {
            return "+84" + phone;
        }
        return phone;
    }

    /**
     * Check TextBee account balance
     * 
     * GET /gateway/devices/{deviceId}
     */
    public Double getBalance() {
        if (apiKey.contains("PLACEHOLDER") || deviceId.contains("PLACEHOLDER")) {
            return null;
        }

        try {
            String url = String.format("%s/gateway/devices/%s", apiUrl, deviceId);

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", apiKey);

            HttpEntity<?> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                if (json.has("data") && json.get("data").has("balance")) {
                    return json.get("data").get("balance").asDouble();
                }
            }
        } catch (Exception e) {
            log.error("[SMS] Error getting balance: {}", e.getMessage());
        }
        return null;
    }
}
