package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Voucher.VoucherRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Voucher.VoucherResponse;
import com.example.bookverseserver.entity.Order_Payment.Voucher;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.entity.User.UserVoucher;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.VoucherMapper;
import com.example.bookverseserver.repository.UserVoucherRepository;
import com.example.bookverseserver.repository.VoucherRepository;
import com.example.bookverseserver.service.discount.DiscountStrategy;
import com.example.bookverseserver.service.discount.DiscountStrategyFactory;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VoucherService {

    VoucherRepository voucherRepository;
    UserVoucherRepository userVoucherRepository;
    VoucherMapper voucherMapper;
    DiscountStrategyFactory discountStrategyFactory;

    public VoucherResponse createVoucher(@Valid VoucherRequest request) {
        log.info("Creating voucher with code: {}", request.getCode());

        // Kiểm tra voucher đã tồn tại chưa
        if (voucherRepository.existsByCode(request.getCode())) {
            log.warn("Voucher code already exists: {}", request.getCode());
            throw new AppException(ErrorCode.VOUCHER_ALREADY_EXISTS);
        }

        // Validate discount type bằng cách thử lấy strategy
        discountStrategyFactory.getStrategy(request.getDiscountType());

        // Tạo voucher mới
        Voucher voucher = voucherMapper.toVoucher(request);
        voucher.setIsActive(true);
        voucher.setValidFrom(LocalDateTime.now());

        // Lưu vào database
        Voucher savedVoucher = voucherRepository.save(voucher);
        log.info("Voucher created successfully with id: {}", savedVoucher.getId());

        // Trả về response với message "Mã hợp lệ"
        return voucherMapper.toVoucherResponse(savedVoucher);
    }

    public VoucherResponse getVoucherByCode(String code) {
        log.info("Fetching voucher with code: {}", code);
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> {
                    log.warn("Voucher not found with code: {}", code);
                    return new AppException(ErrorCode.VOUCHER_NOT_FOUND);
                });

        log.info("Voucher found with code: {}", code);
        return voucherMapper.toVoucherResponse(voucher);
    }

    public void deleteVoucher(Long id) {
        log.info("Deleting voucher with id: {}", id);
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Voucher not found with id: {}", id);
                    return new AppException(ErrorCode.VOUCHER_NOT_FOUND);
                });

        voucherRepository.delete(voucher);
        log.info("Voucher deleted successfully with id: {}", id);
    }
    
    /**
     * Get all vouchers (for admin).
     */
    public List<VoucherResponse> getAllVouchers() {
        log.info("Fetching all vouchers");
        return voucherRepository.findAll().stream()
                .map(voucherMapper::toVoucherResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get active, non-expired vouchers for users to discover.
     * Returns vouchers that are: active, not expired, ordered by discount value (highest first).
     */
    public List<VoucherResponse> getAvailableVouchers() {
        log.info("Fetching available vouchers for users");
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findAll().stream()
                .filter(v -> Boolean.TRUE.equals(v.getIsActive()))
                .filter(v -> v.getValidTo() == null || v.getValidTo().isAfter(now))
                .sorted((a, b) -> b.getDiscountValue().compareTo(a.getDiscountValue()))
                .limit(5) // Show top 5 vouchers
                .map(voucherMapper::toVoucherResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Update an existing voucher (for admin).
     */
    @Transactional
    public VoucherResponse updateVoucher(Long id, VoucherRequest request) {
        log.info("Updating voucher with id: {}", id);
        
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Voucher not found with id: {}", id);
                    return new AppException(ErrorCode.VOUCHER_NOT_FOUND);
                });
        
        // Check if code is being changed and if new code already exists
        if (!voucher.getCode().equals(request.getCode()) && 
            voucherRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.VOUCHER_ALREADY_EXISTS);
        }
        
        // Validate discount type
        discountStrategyFactory.getStrategy(request.getDiscountType());
        
        // Update fields
        voucher.setCode(request.getCode());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setValidTo(request.getValidTo());
        voucher.setMaxUsagePerUser(request.getMaxUsagePerUser());
        if (request.getDescription() != null) {
            voucher.setDescription(request.getDescription());
        }
        
        Voucher updatedVoucher = voucherRepository.save(voucher);
        log.info("Voucher updated successfully with id: {}", id);
        
        return voucherMapper.toVoucherResponse(updatedVoucher);
    }

    public BigDecimal calculateDiscount(String code, BigDecimal orderValue) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        if (!voucher.getIsActive()
                || (voucher.getValidTo() != null && voucher.getValidTo().isBefore(LocalDateTime.now()))) {
            throw new AppException(ErrorCode.VOUCHER_NOT_FOUND);
        }

        DiscountStrategy strategy = discountStrategyFactory.getStrategy(voucher.getDiscountType());
        if (!strategy.isValid(orderValue, voucher.getMinOrderValue())) {
            throw new AppException(ErrorCode.VOUCHER_MIN_ORDER_VALUE_NOT_MET);
        }

        return strategy.calculateDiscount(orderValue, voucher.getDiscountValue(), voucher.getMinOrderValue());
    }
    
    /**
     * Calculate discount with per-user usage validation.
     * Validates that the user hasn't exceeded maxUsagePerUser limit.
     */
    public BigDecimal calculateDiscountForUser(String code, BigDecimal orderValue, Long userId) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        // Validate voucher is active and not expired
        if (!voucher.getIsActive()
                || (voucher.getValidTo() != null && voucher.getValidTo().isBefore(LocalDateTime.now()))) {
            throw new AppException(ErrorCode.VOUCHER_NOT_FOUND);
        }
        
        // Check per-user usage limit
        if (voucher.getMaxUsagePerUser() != null && voucher.getMaxUsagePerUser() > 0) {
            int currentUsage = userVoucherRepository.countUsageByUserAndVoucher(userId, voucher.getId())
                    .orElse(0);
            if (currentUsage >= voucher.getMaxUsagePerUser()) {
                log.warn("User {} has exceeded max usage ({}) for voucher {}", 
                        userId, voucher.getMaxUsagePerUser(), code);
                throw new AppException(ErrorCode.VOUCHER_USAGE_LIMIT_EXCEEDED);
            }
        }

        DiscountStrategy strategy = discountStrategyFactory.getStrategy(voucher.getDiscountType());
        if (!strategy.isValid(orderValue, voucher.getMinOrderValue())) {
            throw new AppException(ErrorCode.VOUCHER_MIN_ORDER_VALUE_NOT_MET);
        }

        return strategy.calculateDiscount(orderValue, voucher.getDiscountValue(), voucher.getMinOrderValue());
    }
    
    /**
     * Record voucher usage for a user after successful order placement.
     * Call this AFTER the order is confirmed/paid.
     */
    @Transactional
    public void recordVoucherUsage(String code, User user) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
        
        // Try to increment existing record
        int updated = userVoucherRepository.incrementUsage(user.getId(), voucher.getId());
        
        if (updated == 0) {
            // No existing record - create new one
            UserVoucher userVoucher = UserVoucher.builder()
                    .user(user)
                    .voucher(voucher)
                    .timesUsed(1)
                    .build();
            userVoucherRepository.save(userVoucher);
        }
        
        log.info("Recorded voucher usage: user={}, voucher={}", user.getId(), code);
    }
}
