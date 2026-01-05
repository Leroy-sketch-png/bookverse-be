package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Voucher.VoucherRequest;
import com.example.bookverseserver.dto.response.Voucher.VoucherResponse;
import com.example.bookverseserver.entity.Order_Payment.Voucher;
import com.example.bookverseserver.enums.DiscountType;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.VoucherMapper;
import com.example.bookverseserver.repository.VoucherRepository;
import com.example.bookverseserver.service.discount.DiscountStrategy;
import com.example.bookverseserver.service.discount.DiscountStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherServiceTest {

  @Mock
  private VoucherRepository voucherRepository;
  @Mock
  private VoucherMapper voucherMapper;
  @Mock
  private DiscountStrategyFactory discountStrategyFactory;
  @Mock
  private DiscountStrategy discountStrategy;

  @InjectMocks
  private VoucherService voucherService;

  private VoucherRequest voucherRequest;
  private Voucher voucher;
  private VoucherResponse voucherResponse;

  @BeforeEach
  void setUp() {
    voucherRequest = new VoucherRequest();
    voucherRequest.setCode("SUMMER20");
    voucherRequest.setDiscountType(DiscountType.PERCENTAGE);
    voucherRequest.setDiscountValue(BigDecimal.valueOf(20));
    voucherRequest.setMinOrderValue(BigDecimal.valueOf(100));
    voucherRequest.setValidTo(LocalDateTime.now().plusDays(30));
    voucherRequest.setMaxUsagePerUser(1);
    voucherRequest.setDescription("Summer sale 20% off");

    voucher = Voucher.builder()
        .id(1L)
        .code("SUMMER20")
        .discountType(DiscountType.PERCENTAGE)
        .discountValue(BigDecimal.valueOf(20))
        .minOrderValue(BigDecimal.valueOf(100))
        .isActive(true)
        .validFrom(LocalDateTime.now())
        .validTo(LocalDateTime.now().plusDays(30))
        .maxUsagePerUser(1)
        .build();

    voucherResponse = VoucherResponse.builder()
        .id(1L)
        .code("SUMMER20")
        .discountType(DiscountType.PERCENTAGE)
        .discountValue(BigDecimal.valueOf(20))
        .minOrderValue(BigDecimal.valueOf(100))
        .validTo(LocalDateTime.now().plusDays(30))
        .maxUsagePerUser(1)
        .build();
  }

  @Test
  void createVoucher_Success() {
    // Given
    when(voucherRepository.existsByCode("SUMMER20")).thenReturn(false);
    when(discountStrategyFactory.getStrategy(DiscountType.PERCENTAGE)).thenReturn(discountStrategy);
    when(voucherMapper.toVoucher(voucherRequest)).thenReturn(voucher);
    when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);
    when(voucherMapper.toVoucherResponse(voucher)).thenReturn(voucherResponse);

    // When
    VoucherResponse result = voucherService.createVoucher(voucherRequest);

    // Then
    assertNotNull(result);
    assertEquals("SUMMER20", result.getCode());
    assertEquals(DiscountType.PERCENTAGE, result.getDiscountType());
    verify(voucherRepository).save(any(Voucher.class));
  }

  @Test
  void createVoucher_DuplicateCode_ThrowsException() {
    // Given
    when(voucherRepository.existsByCode("SUMMER20")).thenReturn(true);

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> voucherService.createVoucher(voucherRequest));
    assertEquals(ErrorCode.VOUCHER_ALREADY_EXISTS, exception.getErrorCode());
    verify(voucherRepository, never()).save(any());
  }

  @Test
  void getVoucherByCode_Success() {
    // Given
    when(voucherRepository.findByCode("SUMMER20")).thenReturn(Optional.of(voucher));
    when(voucherMapper.toVoucherResponse(voucher)).thenReturn(voucherResponse);

    // When
    VoucherResponse result = voucherService.getVoucherByCode("SUMMER20");

    // Then
    assertNotNull(result);
    assertEquals("SUMMER20", result.getCode());
  }

  @Test
  void getVoucherByCode_NotFound_ThrowsException() {
    // Given
    when(voucherRepository.findByCode("INVALID")).thenReturn(Optional.empty());

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> voucherService.getVoucherByCode("INVALID"));
    assertEquals(ErrorCode.VOUCHER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void deleteVoucher_Success() {
    // Given
    when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));

    // When
    voucherService.deleteVoucher(1L);

    // Then
    verify(voucherRepository).delete(voucher);
  }

  @Test
  void deleteVoucher_NotFound_ThrowsException() {
    // Given
    when(voucherRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> voucherService.deleteVoucher(999L));
    assertEquals(ErrorCode.VOUCHER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void calculateDiscount_Success() {
    // Given
    BigDecimal orderValue = BigDecimal.valueOf(200);
    BigDecimal expectedDiscount = BigDecimal.valueOf(40); // 20% of 200

    when(voucherRepository.findByCode("SUMMER20")).thenReturn(Optional.of(voucher));
    when(discountStrategyFactory.getStrategy(DiscountType.PERCENTAGE)).thenReturn(discountStrategy);
    when(discountStrategy.isValid(orderValue, voucher.getMinOrderValue())).thenReturn(true);
    when(discountStrategy.calculateDiscount(orderValue, voucher.getDiscountValue(), voucher.getMinOrderValue()))
        .thenReturn(expectedDiscount);

    // When
    BigDecimal result = voucherService.calculateDiscount("SUMMER20", orderValue);

    // Then
    assertEquals(expectedDiscount, result);
  }

  @Test
  void calculateDiscount_VoucherNotFound_ThrowsException() {
    // Given
    when(voucherRepository.findByCode("INVALID")).thenReturn(Optional.empty());

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> voucherService.calculateDiscount("INVALID", BigDecimal.valueOf(200)));
    assertEquals(ErrorCode.VOUCHER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void calculateDiscount_ExpiredVoucher_ThrowsException() {
    // Given
    voucher.setValidTo(LocalDateTime.now().minusDays(1)); // Expired yesterday
    when(voucherRepository.findByCode("SUMMER20")).thenReturn(Optional.of(voucher));

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> voucherService.calculateDiscount("SUMMER20", BigDecimal.valueOf(200)));
    assertEquals(ErrorCode.VOUCHER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void calculateDiscount_InactiveVoucher_ThrowsException() {
    // Given
    voucher.setIsActive(false);
    when(voucherRepository.findByCode("SUMMER20")).thenReturn(Optional.of(voucher));

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> voucherService.calculateDiscount("SUMMER20", BigDecimal.valueOf(200)));
    assertEquals(ErrorCode.VOUCHER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void calculateDiscount_MinOrderNotMet_ThrowsException() {
    // Given
    BigDecimal orderValue = BigDecimal.valueOf(50); // Below min order value of 100

    when(voucherRepository.findByCode("SUMMER20")).thenReturn(Optional.of(voucher));
    when(discountStrategyFactory.getStrategy(DiscountType.PERCENTAGE)).thenReturn(discountStrategy);
    when(discountStrategy.isValid(orderValue, voucher.getMinOrderValue())).thenReturn(false);

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> voucherService.calculateDiscount("SUMMER20", orderValue));
    assertEquals(ErrorCode.VOUCHER_MIN_ORDER_VALUE_NOT_MET, exception.getErrorCode());
  }
}
