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
}
