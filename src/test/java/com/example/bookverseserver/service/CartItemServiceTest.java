package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.CartItem.CartItemRequest;
import com.example.bookverseserver.dto.response.CartItem.CartItemResponse;
import com.example.bookverseserver.entity.Order_Payment.Cart;
import com.example.bookverseserver.entity.Order_Payment.CartItem;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.CartItemRepository;
import com.example.bookverseserver.repository.CartRepository;
import com.example.bookverseserver.repository.ListingRepository;
import com.example.bookverseserver.repository.UserRepository;
import com.example.bookverseserver.service.discount.DiscountStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartItemServiceTest {

  @Mock
  private CartItemRepository cartItemRepository;
  @Mock
  private CartRepository cartRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ListingRepository listingRepository;
  @Mock
  private DiscountStrategyFactory discountStrategyFactory;

  @InjectMocks
  private CartItemService cartItemService;

  private User testUser;
  private Cart testCart;
  private Listing testListing;
  private CartItem testCartItem;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);

    testCart = Cart.builder()
        .id(1L)
        .user(testUser)
        .totalPrice(BigDecimal.valueOf(100))
        .cartItems(new HashSet<>())
        .build();

    testListing = Listing.builder()
        .id(1L)
        .price(BigDecimal.valueOf(50))
        .quantity(10)
        .build();

    testCartItem = CartItem.builder()
        .id(1L)
        .cart(testCart)
        .listing(testListing)
        .quantity(2)
        .build();
  }

  @Test
  void createCartItem_NewItem_Success() {
    // Given
    CartItemRequest request = new CartItemRequest(1L, 2);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(listingRepository.findById(1L)).thenReturn(Optional.of(testListing));
    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
    when(cartItemRepository.findByCartIdAndListingId(1L, 1L)).thenReturn(Optional.empty());
    when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    // When
    CartItemResponse result = cartItemService.createCartItem(1L, request);

    // Then
    assertNotNull(result);
    verify(cartItemRepository).save(any(CartItem.class));
    verify(cartRepository).save(any(Cart.class));
  }

  @Test
  void createCartItem_ExistingItem_IncreasesQuantity() {
    // Given
    CartItemRequest request = new CartItemRequest(1L, 2);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(listingRepository.findById(1L)).thenReturn(Optional.of(testListing));
    when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
    when(cartItemRepository.findByCartIdAndListingId(1L, 1L)).thenReturn(Optional.of(testCartItem));
    when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    // When
    CartItemResponse result = cartItemService.createCartItem(1L, request);

    // Then
    assertNotNull(result);
    assertEquals(4, testCartItem.getQuantity()); // 2 + 2 = 4
  }

  @Test
  void createCartItem_InsufficientStock_ThrowsException() {
    // Given
    CartItemRequest request = new CartItemRequest(1L, 100); // More than stock (10)

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(listingRepository.findById(1L)).thenReturn(Optional.of(testListing));

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> cartItemService.createCartItem(1L, request));
    assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
  }

  @Test
  void createCartItem_UserNotFound_ThrowsException() {
    // Given
    CartItemRequest request = new CartItemRequest(1L, 2);
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> cartItemService.createCartItem(999L, request));
    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void createCartItem_ListingNotFound_ThrowsException() {
    // Given
    CartItemRequest request = new CartItemRequest(999L, 2);
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(listingRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> cartItemService.createCartItem(1L, request));
    assertEquals(ErrorCode.LISTING_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void updateCartItem_Success() {
    // Given
    when(cartItemRepository.findByCartUserIdAndListingId(1L, 1L)).thenReturn(Optional.of(testCartItem));
    when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

    // When
    CartItemResponse result = cartItemService.updateCartItem(1L, 1L, 5);

    // Then
    assertNotNull(result);
    assertEquals(5, testCartItem.getQuantity());
  }

  @Test
  void updateCartItem_InvalidQuantity_ThrowsException() {
    // Given - quantity <= 0

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> cartItemService.updateCartItem(1L, 1L, 0));
    assertEquals(ErrorCode.INVALID_QUANTITY, exception.getErrorCode());
  }

  @Test
  void updateCartItem_InsufficientStock_ThrowsException() {
    // Given
    when(cartItemRepository.findByCartUserIdAndListingId(1L, 1L)).thenReturn(Optional.of(testCartItem));

    // When & Then - quantity > stock (10)
    AppException exception = assertThrows(AppException.class,
        () -> cartItemService.updateCartItem(1L, 1L, 100));
    assertEquals(ErrorCode.INSUFFICIENT_STOCK, exception.getErrorCode());
  }

  @Test
  void updateCartItem_CartItemNotFound_ThrowsException() {
    // Given
    when(cartItemRepository.findByCartUserIdAndListingId(1L, 999L)).thenReturn(Optional.empty());

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> cartItemService.updateCartItem(1L, 999L, 5));
    assertEquals(ErrorCode.CART_ITEM_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void deleteCartItem_Success() {
    // Given
    when(cartItemRepository.findByCartUserIdAndListingId(1L, 1L)).thenReturn(Optional.of(testCartItem));

    // When
    CartItemResponse result = cartItemService.deleteCartItem(1L, 1L);

    // Then
    assertNotNull(result);
    verify(cartItemRepository).delete(testCartItem);
  }

  @Test
  void deleteCartItem_CartItemNotFound_ThrowsException() {
    // Given
    when(cartItemRepository.findByCartUserIdAndListingId(1L, 999L)).thenReturn(Optional.empty());

    // When & Then
    AppException exception = assertThrows(AppException.class,
        () -> cartItemService.deleteCartItem(1L, 999L));
    assertEquals(ErrorCode.CART_ITEM_NOT_FOUND, exception.getErrorCode());
  }
}
