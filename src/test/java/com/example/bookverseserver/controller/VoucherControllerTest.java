package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Voucher.VoucherRequest;
import com.example.bookverseserver.dto.response.Voucher.VoucherResponse;
import com.example.bookverseserver.enums.DiscountType;
import com.example.bookverseserver.service.AuthenticationService;
import com.example.bookverseserver.service.VoucherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VoucherController.class)
class VoucherControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private VoucherService voucherService;

  @MockBean
  private AuthenticationService authenticationService;

  private VoucherRequest voucherRequest;
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
  @WithMockUser(roles = "ADMIN")
  void createVoucher_AsAdmin_Returns201() throws Exception {
    // Arrange
    when(voucherService.createVoucher(any(VoucherRequest.class))).thenReturn(voucherResponse);

    // Act & Assert
    mockMvc.perform(post("/api/vouchers")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(voucherRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code", is(201)))
        .andExpect(jsonPath("$.message", is("Voucher created successfully!")))
        .andExpect(jsonPath("$.result.code", is("SUMMER20")));
  }

  @Test
  void createVoucher_Unauthenticated_RedirectsToOAuth2() throws Exception {
    // Act & Assert - No authentication
    mockMvc.perform(post("/api/vouchers")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(voucherRequest)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/oauth2/authorization/google"));
  }

  @Test
  @WithMockUser
  void getVoucher_ReturnsVoucher() throws Exception {
    // Arrange
    when(voucherService.getVoucherByCode("SUMMER20")).thenReturn(voucherResponse);

    // Act & Assert
    mockMvc.perform(get("/api/vouchers/{code}", "SUMMER20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(200)))
        .andExpect(jsonPath("$.message", is("Voucher found!")))
        .andExpect(jsonPath("$.result.code", is("SUMMER20")))
        .andExpect(jsonPath("$.result.discountType", is("PERCENTAGE")));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deleteVoucher_AsAdmin_Returns204() throws Exception {
    // Act & Assert
    mockMvc.perform(delete("/api/vouchers/{id}", 1L)
        .with(csrf()))
        .andExpect(status().isNoContent());

    verify(voucherService).deleteVoucher(1L);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createVoucher_InvalidRequest_Returns400() throws Exception {
    // Arrange - Missing required fields
    VoucherRequest invalidRequest = new VoucherRequest();
    invalidRequest.setCode(""); // Invalid: blank
    invalidRequest.setDiscountValue(BigDecimal.valueOf(-10)); // Invalid: negative

    // Act & Assert
    mockMvc.perform(post("/api/vouchers")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }
}
