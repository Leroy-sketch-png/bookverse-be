package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Voucher.VoucherRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Voucher.VoucherResponse;
import com.example.bookverseserver.entity.Order_Payment.Voucher;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.VoucherMapper;
import com.example.bookverseserver.repository.VoucherRepository;
import com.example.bookverseserver.service.discount.DiscountStrategy;
import com.example.bookverseserver.service.discount.DiscountStrategyFactory;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VoucherService {

    VoucherRepository voucherRepository;
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
     * Tính toán số tiền giảm giá cho đơn hàng
     * Sử dụng Strategy Pattern để chọn thuật toán giảm giá phù hợp
     * @param voucherCode Mã voucher
     * @param orderAmount Tổng giá trị đơn hàng
     * @return Số tiền được giảm
     */
    public BigDecimal calculateDiscount(String voucherCode, BigDecimal orderAmount) {
        log.info("Calculating discount for voucher: {} with order amount: {}", voucherCode, orderAmount);

        Voucher voucher = voucherRepository.findByCode(voucherCode)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        // Kiểm tra voucher còn hiệu lực
        if (!voucher.getIsActive()) {
            log.warn("Voucher {} is not active", voucherCode);
            return BigDecimal.ZERO;
        }

        LocalDateTime now = LocalDateTime.now();
        if (voucher.getValidTo() != null && now.isAfter(voucher.getValidTo())) {
            log.warn("Voucher {} has expired", voucherCode);
            return BigDecimal.ZERO;
        }

        if (voucher.getValidFrom() != null && now.isBefore(voucher.getValidFrom())) {
            log.warn("Voucher {} is not yet valid", voucherCode);
            return BigDecimal.ZERO;
        }

        // Sử dụng Strategy Pattern để tính giảm giá
        DiscountStrategy strategy = discountStrategyFactory.getStrategy(voucher.getDiscountType());
        BigDecimal discount = strategy.calculateDiscount(
                orderAmount,
                voucher.getDiscountValue(),
                voucher.getMinOrderValue()
        );

        log.info("Discount calculated: {} for voucher: {}", discount, voucherCode);
        return discount;
    }

    /**
     * Kiểm tra voucher có hợp lệ với đơn hàng không
     * @param voucherCode Mã voucher
     * @param orderAmount Tổng giá trị đơn hàng
     * @return true nếu hợp lệ
     */
    public boolean isVoucherValid(String voucherCode, BigDecimal orderAmount) {
        try {
            Voucher voucher = voucherRepository.findByCode(voucherCode)
                    .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

            if (!voucher.getIsActive()) {
                return false;
            }

            LocalDateTime now = LocalDateTime.now();
            if (voucher.getValidTo() != null && now.isAfter(voucher.getValidTo())) {
                return false;
            }

            if (voucher.getValidFrom() != null && now.isBefore(voucher.getValidFrom())) {
                return false;
            }

            DiscountStrategy strategy = discountStrategyFactory.getStrategy(voucher.getDiscountType());
            return strategy.isValid(orderAmount, voucher.getMinOrderValue());
        } catch (Exception e) {
            log.error("Error validating voucher: {}", voucherCode, e);
            return false;
        }
    }
}
