package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.Cart.CartResponse;
import com.example.bookverseserver.dto.response.Cart.CartSummary;
import com.example.bookverseserver.entity.Order_Payment.Cart;
import com.example.bookverseserver.entity.Order_Payment.Voucher;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.DiscountType;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.CartItemMapper;
import com.example.bookverseserver.repository.CartItemRepository;
import com.example.bookverseserver.repository.CartRepository;
import com.example.bookverseserver.repository.UserRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

  @Mock
  private CartRepository cartRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private VoucherRepository voucherRepository;
  @Mock
  private CartItemRepository cartItemRepository;
  @Mock
  private CartItemMapper cartItemMapper;
  @Mock
  private DiscountStrategyFactory discountStrategyFactory;
  @Mock
  private DiscountStrategy discountStrategy;

  @InjectMocks
  private CartService cartService;

  private User testUser;
  private Cart testCart;
  private Voucher testVoucher;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");

    testCart = Cart.builder()
        .id(1L)
        .user(testUser)
        .totalPrice(BigDecimal.valueOf(100))
        .cartItems(new HashSet<>())
        .build();

    testVoucher = Voucher.builder()
        .id(1L)
        .code("SUMMER20")
        .discountType(DiscountType.PERCENTAGE)
        .discountValue(BigDecimal.valueOf(20))
        .minOrderValue(BigDecimal.valueOf(50))
        .isActive(true)
        .validTo(LocalDateTime.now().plusDays(30))
        .build();
  }

  @Test
  void getCartByUserId_ExistingCart_ReturnsCart() {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

    // When
    CartResponse result = cartService.getCartByUserId(1L);

    // Then
    assertNotNull(result);
    assertEquals(1L, result.id());
    // CartResponse uses nested summary per Vision API_CONTRACTS.md
    assertNotNull(result.summary());
    assertEquals(BigDecimal.valueOf(100), result.summary().subtotal());
  }

  @Test
  void getCartByUserId_NoExistingCart_CreatesNewCart() {
    // Given
    Cart newCart = Cart.builder()
        .id(2L)
        .user(testUser)
        .totalPrice(BigDecimal.ZERO)
        .cartItems(new HashSet<>())
        .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
    when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

    // When
    CartResponse result = cartService.getCartByUserId(1L);

    // Then
    assertNotNull(result);
    assertNotNull(result.summary());
    assertEquals(BigDecimal.ZERO, result.summary().subtotal());
    verify(cartRepository).save(any(Cart.class));
  }

  @Test
  void getCartByUserId_UserNotFound_ThrowsException() {
    // Given
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> cartService.getCartByUserId(999L));
    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void applyVoucherToCart_Success() {
    // Given
    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
    when(voucherRepository.findByCode("SUMMER20")).thenReturn(Optional.of(testVoucher));
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
    when(discountStrategyFactory.getStrategy(DiscountType.PERCENTAGE)).thenReturn(discountStrategy);
    when(discountStrategy.calculateDiscount(any(), any(), any())).thenReturn(BigDecimal.valueOf(20));

    // When
    CartResponse result = cartService.applyVoucherToCart(1L, "SUMMER20");

    // Then
    assertNotNull(result);
    verify(cartRepository).save(testCart);
  }

  @Test
  void applyVoucherToCart_CartNotFound_ThrowsException() {
    // Given
    when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> cartService.applyVoucherToCart(1L, "SUMMER20"));
    assertEquals(ErrorCode.CART_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void applyVoucherToCart_VoucherNotFound_ThrowsException() {
    // Given
    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
    when(voucherRepository.findByCode("INVALID")).thenReturn(Optional.empty());

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> cartService.applyVoucherToCart(1L, "INVALID"));
    assertEquals(ErrorCode.VOUCHER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void applyVoucherToCart_VoucherExpired_ThrowsException() {
    // Given
    testVoucher.setValidTo(LocalDateTime.now().minusDays(1));
    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
    when(voucherRepository.findByCode("SUMMER20")).thenReturn(Optional.of(testVoucher));

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> cartService.applyVoucherToCart(1L, "SUMMER20"));
    assertEquals(ErrorCode.VOUCHER_EXPIRED, exception.getErrorCode());
  }

  @Test
  void applyVoucherToCart_VoucherInactive_ThrowsException() {
    // Given
    testVoucher.setIsActive(false);
    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
    when(voucherRepository.findByCode("SUMMER20")).thenReturn(Optional.of(testVoucher));

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> cartService.applyVoucherToCart(1L, "SUMMER20"));
    assertEquals(ErrorCode.VOUCHER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void applyVoucherToCart_MinOrderNotMet_ThrowsException() {
    // Given
    testCart.setTotalPrice(BigDecimal.valueOf(30)); // Below min order value of 50
    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
    when(voucherRepository.findByCode("SUMMER20")).thenReturn(Optional.of(testVoucher));

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> cartService.applyVoucherToCart(1L, "SUMMER20"));
    assertEquals(ErrorCode.VOUCHER_MIN_ORDER_VALUE_NOT_MET, exception.getErrorCode());
  }

  @Test
  void removeVoucherFromCart_Success() {
    // Given
    testCart.setVoucher(testVoucher);
    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    // When
    CartResponse result = cartService.removeVoucherFromCart(1L);

    // Then
    assertNotNull(result);
    assertNull(testCart.getVoucher());
    verify(cartRepository).save(testCart);
  }

  @Test
  void clearCart_Success() {
    // Given
    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

    // When
    cartService.clearCart(1L);

    // Then
    verify(cartItemRepository).deleteAllByCartId(testCart.getId());
    verify(cartRepository).save(testCart);
    assertEquals(BigDecimal.ZERO, testCart.getTotalPrice());
  }
}
