package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.response.Cart.CartResponse;
import com.example.bookverseserver.service.AuthenticationService;
import com.example.bookverseserver.service.CartService;
import com.example.bookverseserver.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CartService cartService;

  @MockBean
  private SecurityUtils securityUtils;

  @MockBean
  private AuthenticationService authenticationService;

  private final Long TEST_USER_ID = 1L;
  private CartResponse cartResponse;

  @BeforeEach
  void setUp() {
    cartResponse = CartResponse.builder()
        .id(1L)
        .userId(TEST_USER_ID)
        .cartItems(Set.of())
        .subtotal(BigDecimal.valueOf(100))
        .tax(BigDecimal.valueOf(9))
        .shipping(BigDecimal.ZERO)
        .discount(BigDecimal.ZERO)
        .total(BigDecimal.valueOf(109))
        .itemCount(2)
        .build();
  }

  @Test
  @WithMockUser
  void getCart_ReturnsCartSuccessfully() throws Exception {
    // Arrange
    when(securityUtils.getCurrentUserId(any(Authentication.class))).thenReturn(TEST_USER_ID);
    when(cartService.getCartByUserId(TEST_USER_ID)).thenReturn(cartResponse);

    // Act & Assert
    mockMvc.perform(get("/api/v1/cart"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(200)))
        .andExpect(jsonPath("$.message", is("Cart retrieved successfully")))
        .andExpect(jsonPath("$.result.subtotal", is(100)))
        .andExpect(jsonPath("$.result.itemCount", is(2)));
  }

  @Test
  void getCart_Unauthenticated_RedirectsToOAuth2() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/v1/cart"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/oauth2/authorization/google"));
  }

  @Test
  @WithMockUser
  void applyVoucher_Success() throws Exception {
    // Arrange
    CartResponse cartWithVoucher = CartResponse.builder()
        .id(1L)
        .userId(TEST_USER_ID)
        .subtotal(BigDecimal.valueOf(100))
        .discount(BigDecimal.valueOf(20))
        .total(BigDecimal.valueOf(89))
        .build();

    when(securityUtils.getCurrentUserId(any(Authentication.class))).thenReturn(TEST_USER_ID);
    when(cartService.applyVoucherToCart(eq(TEST_USER_ID), eq("SUMMER20"))).thenReturn(cartWithVoucher);

    // Act & Assert
    mockMvc.perform(post("/api/v1/cart/voucher")
        .with(csrf())
        .param("voucherCode", "SUMMER20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(200)))
        .andExpect(jsonPath("$.message", is("Voucher applied successfully")))
        .andExpect(jsonPath("$.result.discount", is(20)));
  }

  @Test
  @WithMockUser
  void removeVoucher_Success() throws Exception {
    // Arrange
    when(securityUtils.getCurrentUserId(any(Authentication.class))).thenReturn(TEST_USER_ID);
    when(cartService.removeVoucherFromCart(TEST_USER_ID)).thenReturn(cartResponse);

    // Act & Assert
    mockMvc.perform(patch("/api/v1/cart/voucher")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(200)))
        .andExpect(jsonPath("$.message", is("Voucher removed successfully")));
  }
}
