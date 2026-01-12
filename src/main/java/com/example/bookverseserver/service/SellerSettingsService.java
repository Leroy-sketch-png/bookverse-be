package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.SellerSettingsUpdateRequest;
import com.example.bookverseserver.dto.response.SellerSettingsResponse;
import com.example.bookverseserver.entity.User.SellerSettings;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.SellerSettingsRepository;
import com.example.bookverseserver.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SellerSettingsService {
    
    SellerSettingsRepository sellerSettingsRepository;
    UserRepository userRepository;

    /**
     * Get seller settings for authenticated user.
     * Creates default settings if none exist.
     */
    @Transactional
    public SellerSettingsResponse getSettings(Long userId) {
        SellerSettings settings = sellerSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
        
        return toResponse(settings);
    }

    /**
     * Update seller settings.
     * Only updates provided fields (partial update).
     */
    @Transactional
    public SellerSettingsResponse updateSettings(Long userId, SellerSettingsUpdateRequest request) {
        SellerSettings settings = sellerSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
        
        // Merge shipping settings
        if (request.getShipping() != null) {
            Map<String, Object> merged = new HashMap<>(settings.getShippingSettings());
            merged.putAll(request.getShipping());
            settings.setShippingSettings(merged);
        }
        
        // Merge notification settings
        if (request.getNotifications() != null) {
            Map<String, Object> merged = new HashMap<>(settings.getNotificationSettings());
            merged.putAll(request.getNotifications());
            settings.setNotificationSettings(merged);
        }
        
        // Merge privacy settings
        if (request.getPrivacy() != null) {
            Map<String, Object> merged = new HashMap<>(settings.getPrivacySettings());
            merged.putAll(request.getPrivacy());
            settings.setPrivacySettings(merged);
        }
        
        SellerSettings saved = sellerSettingsRepository.save(settings);
        log.info("Updated seller settings for user {}", userId);
        
        return toResponse(saved);
    }

    /**
     * Create default settings for a new seller.
     */
    private SellerSettings createDefaultSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        SellerSettings settings = SellerSettings.builder()
                .user(user)
                .build();
        
        return sellerSettingsRepository.save(settings);
    }

    /**
     * Convert entity to response DTO.
     */
    private SellerSettingsResponse toResponse(SellerSettings settings) {
        return SellerSettingsResponse.builder()
                .id(settings.getId())
                .shipping(settings.getShippingSettings())
                .notifications(settings.getNotificationSettings())
                .privacy(settings.getPrivacySettings())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
}
