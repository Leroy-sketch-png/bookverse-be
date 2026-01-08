package com.example.bookverseserver.controller;

import com.example.bookverseserver.configuration.CustomJwtDecoder;
import com.example.bookverseserver.dto.request.CartItem.CartItemRequest;
import com.example.bookverseserver.dto.response.CartItem.CartItemResponse;
import com.example.bookverseserver.service.AuthenticationService;
import com.example.bookverseserver.service.CartItemService;
import com.example.bookverseserver.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartItemController.class)
class CartItemControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private CartItemService cartItemService;

  @MockBean
  private SecurityUtils securityUtils;

  @MockBean
  private AuthenticationService authenticationService;

  @MockBean
  private CustomJwtDecoder customJwtDecoder;

  private final Long TEST_USER_ID = 1L;
  private final Long TEST_LISTING_ID = 1L;
  private CartItemResponse cartItemResponse;

  @BeforeEach
  void setUp() {
    cartItemResponse = CartItemResponse.builder()
        .totalPrice(BigDecimal.valueOf(150))
        .discount(BigDecimal.ZERO)
        .build();
  }

  @Test
  @WithMockUser
  void createCartItem_Success() throws Exception {
    // Arrange
    CartItemRequest request = new CartItemRequest(TEST_LISTING_ID, 2);

    when(securityUtils.getCurrentUserId(any(Authentication.class))).thenReturn(TEST_USER_ID);
    when(cartItemService.createCartItem(eq(TEST_USER_ID), any(CartItemRequest.class)))
        .thenReturn(cartItemResponse);

    // Act & Assert
    mockMvc.perform(post("/api/cart/items")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(200)))
        .andExpect(jsonPath("$.message", is("Cart item created successfully")))
        .andExpect(jsonPath("$.result.totalPrice", is(150)));
  }

  @Test
  void createCartItem_Unauthenticated_RedirectsToOAuth2() throws Exception {
    // Arrange
    CartItemRequest request = new CartItemRequest(TEST_LISTING_ID, 2);

    // Act & Assert
    mockMvc.perform(post("/api/cart/items")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/oauth2/authorization/google"));
  }

  @Test
  @WithMockUser
  void updateCartItem_Success() throws Exception {
    // Arrange
    when(securityUtils.getCurrentUserId(any(Authentication.class))).thenReturn(TEST_USER_ID);
    when(cartItemService.updateCartItem(TEST_USER_ID, TEST_LISTING_ID, 5)).thenReturn(cartItemResponse);

    // Act & Assert
    mockMvc.perform(put("/api/cart/items/{listingId}", TEST_LISTING_ID)
        .with(csrf())
        .param("quantity", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(200)))
        .andExpect(jsonPath("$.message", is("Cart item updated successfully")));
  }

  @Test
  @WithMockUser
  void deleteCartItem_Success() throws Exception {
    // Arrange
    when(securityUtils.getCurrentUserId(any(Authentication.class))).thenReturn(TEST_USER_ID);
    when(cartItemService.deleteCartItem(TEST_USER_ID, TEST_LISTING_ID)).thenReturn(cartItemResponse);

    // Act & Assert
    mockMvc.perform(delete("/api/cart/items/{listingId}", TEST_LISTING_ID)
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(200)))
        .andExpect(jsonPath("$.message", is("Cart item deleted successfully")));
  }
}
