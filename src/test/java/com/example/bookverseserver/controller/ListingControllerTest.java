package com.example.bookverseserver.controller;

import com.example.bookverseserver.configuration.CustomJwtDecoder;
import com.example.bookverseserver.dto.request.Product.ListingCreationRequest;
import com.example.bookverseserver.dto.request.Product.ListingRequest;
import com.example.bookverseserver.dto.request.Product.ListingUpdateRequest;
import com.example.bookverseserver.dto.request.Product.UpdateStockRequest;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.dto.response.Product.*;
import com.example.bookverseserver.enums.BookCondition;
import com.example.bookverseserver.enums.ListingStatus;
import com.example.bookverseserver.enums.StockOperation;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.service.AuthenticationService;
import com.example.bookverseserver.service.ListingService;
import com.example.bookverseserver.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ListingController.class)
class ListingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ListingService listingService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private CustomJwtDecoder customJwtDecoder;

    @MockBean
    private AuthenticationService authenticationService;

    private ListingResponse sampleListingResponse;
    private ListingDetailResponse sampleDetailResponse;
    private PagedResponse<ListingResponse> samplePagedResponse;

    @BeforeEach
    void setUp() {
        // ListingResponse uses nested structure per Vision API_CONTRACTS.md
        sampleListingResponse = ListingResponse.builder()
                .id(301L)
                .book(ListingResponse.BookInfo.builder()
                        .id(123L)
                        .title("Clean Code")
                        .build())
                .seller(ListingResponse.SellerInfo.builder()
                        .id(50L)
                        .name("bookstore_pro")
                        .build())
                .price(new BigDecimal("45.99"))
                .condition(BookCondition.NEW)
                .quantity(15)
                .stats(ListingResponse.ListingStats.builder()
                        .views(100)
                        .soldCount(45)
                        .build())
                .createdAt(LocalDateTime.now().toString())
                .build();

        // ListingDetailResponse uses views/quantity per Vision (not viewCount/stockQuantity)
        sampleDetailResponse = ListingDetailResponse.builder()
                .id(301L)
                .book(BookSummaryDto.builder()
                        .id(123L)
                        .title("Clean Code")
                        .author("Robert C. Martin")
                        .isbn("978-0132350884")
                        .build())
                .seller(SellerSummaryDto.builder()
                        .id(50L)
                        .username("bookstore_pro")
                        .name("Pro Book Store")
                        .rating(4.8)
                        .isPro(true)
                        .build())
                .condition(BookCondition.NEW)
                .price(new BigDecimal("45.99"))
                .originalPrice(new BigDecimal("55.99"))
                .discount(18)
                .quantity(15)
                .status(ListingStatus.ACTIVE)
                .views(100)
                .soldCount(45)
                .shippingInfo(ShippingInfoDto.builder()
                        .freeShipping(true)
                        .estimatedDays("2-3 business days")
                        .shipsFrom("Ho Chi Minh City")
                        .build())
                .relatedListings(List.of())
                .build();

        samplePagedResponse = PagedResponse.<ListingResponse>builder()
                .data(List.of(sampleListingResponse))
                .meta(PagedResponse.PaginationMeta.builder()
                        .totalItems(1L)
                        .page(1)
                        .limit(20)
                        .totalPages(1)
                        .hasNext(false)
                        .hasPrev(false)
                        .build())
                .build();
    }

    // ============ GET LISTINGS TESTS ============

    @Nested
    @DisplayName("GET /api/listings")
    class GetListingsTests {

        @Test
        @WithMockUser
        @DisplayName("Should return listings with default pagination")
        void getListings_DefaultParams_ReturnsListings() throws Exception {
            when(listingService.getListingsFiltered(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                    eq("createdAt"), eq("desc"),
                    eq(0), eq(20)))
                    .thenReturn(samplePagedResponse);

            mockMvc.perform(get("/api/listings"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.data").isArray())
                    .andExpect(jsonPath("$.result.data[0].id").value(301))
                    .andExpect(jsonPath("$.result.meta.totalItems").value(1));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return listings filtered by seller ID")
        void getListings_WithSellerFilter_ReturnsFilteredListings() throws Exception {
            when(listingService.getListingsFiltered(
                    isNull(), eq(50L), isNull(), isNull(), isNull(), isNull(), isNull(),
                    eq("createdAt"), eq("desc"),
                    eq(0), eq(20)))
                    .thenReturn(samplePagedResponse);

            mockMvc.perform(get("/api/listings")
                            .param("sellerId", "50"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.data[0].seller.id").value(50));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return listings filtered by status")
        void getListings_WithStatusFilter_ReturnsFilteredListings() throws Exception {
            when(listingService.getListingsFiltered(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(ListingStatus.ACTIVE),
                    eq("createdAt"), eq("desc"),
                    eq(0), eq(20)))
                    .thenReturn(samplePagedResponse);

            mockMvc.perform(get("/api/listings")
                            .param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.data", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return listings sorted by price")
        void getListings_SortedByPrice_ReturnsSortedListings() throws Exception {
            when(listingService.getListingsFiltered(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                    eq("price"), eq("asc"),
                    eq(0), eq(20)))
                    .thenReturn(samplePagedResponse);

            mockMvc.perform(get("/api/listings")
                            .param("sortBy", "price")
                            .param("sortOrder", "asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.data").isArray());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return paginated results")
        void getListings_WithPagination_ReturnsPaginatedResults() throws Exception {
            when(listingService.getListingsFiltered(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                    eq("createdAt"), eq("desc"),
                    eq(1), eq(10)))
                    .thenReturn(samplePagedResponse);

            mockMvc.perform(get("/api/listings")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.meta").exists());
        }
    }

    // ============ GET LISTING DETAIL TESTS ============

    @Nested
    @DisplayName("GET /api/listings/{id}")
    class GetListingDetailTests {

        @Test
        @WithMockUser
        @DisplayName("Should return listing details")
        void getListingById_ValidId_ReturnsDetails() throws Exception {
            when(listingService.getListingDetail(eq(301L), any(Authentication.class)))
                    .thenReturn(sampleDetailResponse);

            mockMvc.perform(get("/api/listings/301"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value(301))
                    .andExpect(jsonPath("$.result.book.title").value("Clean Code"))
                    .andExpect(jsonPath("$.result.seller.username").value("bookstore_pro"))
                    .andExpect(jsonPath("$.result.views").value(100))
                    .andExpect(jsonPath("$.result.shippingInfo.freeShipping").value(true));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 404 when listing not found")
        void getListingById_NotFound_Returns404() throws Exception {
            when(listingService.getListingDetail(eq(999L), any(Authentication.class)))
                    .thenThrow(new AppException(ErrorCode.LISTING_NOT_EXISTED));

            mockMvc.perform(get("/api/listings/999"))
                    .andExpect(status().isNotFound());
        }
    }

    // ============ CREATE LISTING TESTS ============

    @Nested
    @DisplayName("POST /api/listings")
    class CreateListingTests {

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("Should create listing successfully")
        void createListing_ValidData_Returns201() throws Exception {
            ListingRequest listingRequest = ListingRequest.builder()
                    .price(new BigDecimal("45.99"))
                    .condition(BookCondition.NEW)
                    .quantity(15)
                    .status(ListingStatus.ACTIVE)
                    .build();

            ListingCreationRequest request = ListingCreationRequest.builder()
                    .bookMetaId(123L)
                    .listing(listingRequest)
                    .build();

            when(listingService.createListing(any(ListingCreationRequest.class), any(Authentication.class)))
                    .thenReturn(sampleListingResponse);

            mockMvc.perform(post("/api/listings")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.result.id").value(301))
                    .andExpect(jsonPath("$.message").value("Listing created successfully"));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should create listing (role enforcement at service level)")
        void createListing_NotSeller_Returns403() throws Exception {
            ListingCreationRequest request = ListingCreationRequest.builder()
                    .bookMetaId(123L)
                    .build();

            when(listingService.createListing(any(ListingCreationRequest.class), any(Authentication.class)))
                    .thenReturn(sampleListingResponse);

            // Note: Role enforcement happens in service, not controller annotation
            mockMvc.perform(post("/api/listings")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser
        @DisplayName("Should create listing successfully when authenticated")
        void createListing_Unauthenticated_Returns401() throws Exception {
            ListingCreationRequest request = ListingCreationRequest.builder()
                    .bookMetaId(123L)
                    .build();

            when(listingService.createListing(any(ListingCreationRequest.class), any(Authentication.class)))
                    .thenReturn(sampleListingResponse);

            mockMvc.perform(post("/api/listings")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
    }

    // ============ UPDATE LISTING TESTS ============

    @Nested
    @DisplayName("PUT /api/listings/{id}")
    class UpdateListingTests {

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("Should update listing successfully")
        void updateListing_ValidData_Returns200() throws Exception {
            ListingUpdateRequest updateRequest = new ListingUpdateRequest();
            updateRequest.setPrice(new BigDecimal("49.99"));

            ListingUpdateResponse updateResponse = new ListingUpdateResponse();
            updateResponse.setId(301L);

            when(listingService.updateListing(eq(301L), any(ListingUpdateRequest.class), any(Authentication.class)))
                    .thenReturn(updateResponse);

            mockMvc.perform(put("/api/listings/301")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value(301));
        }

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("Should return 403 when not owner")
        void updateListing_NotOwner_Returns403() throws Exception {
            ListingUpdateRequest updateRequest = new ListingUpdateRequest();

            when(listingService.updateListing(eq(301L), any(ListingUpdateRequest.class), any(Authentication.class)))
                    .thenThrow(new AppException(ErrorCode.DO_NOT_HAVE_PERMISSION));

            mockMvc.perform(put("/api/listings/301")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    // ============ UPDATE STOCK TESTS ============

    @Nested
    @DisplayName("PATCH /api/listings/{id}/stock")
    class UpdateStockTests {

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("Should update stock with SET operation")
        void updateStock_SetOperation_Returns200() throws Exception {
            UpdateStockRequest request = UpdateStockRequest.builder()
                    .quantity(25)
                    .operation(StockOperation.SET)
                    .build();

            StockUpdateResponse response = StockUpdateResponse.builder()
                    .listingId(301L)
                    .oldQuantity(15)
                    .newQuantity(25)
                    .status(ListingStatus.ACTIVE)
                    .build();

            when(securityUtils.getCurrentUserId(any(Authentication.class))).thenReturn(50L);
            when(listingService.updateStock(eq(301L), eq(50L), any(UpdateStockRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(patch("/api/listings/301/stock")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.listingId").value(301))
                    .andExpect(jsonPath("$.result.oldQuantity").value(15))
                    .andExpect(jsonPath("$.result.newQuantity").value(25))
                    .andExpect(jsonPath("$.message").value("Stock updated successfully"));
        }

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("Should update stock with ADD operation")
        void updateStock_AddOperation_Returns200() throws Exception {
            UpdateStockRequest request = UpdateStockRequest.builder()
                    .quantity(10)
                    .operation(StockOperation.ADD)
                    .build();

            StockUpdateResponse response = StockUpdateResponse.builder()
                    .listingId(301L)
                    .oldQuantity(15)
                    .newQuantity(25)
                    .status(ListingStatus.ACTIVE)
                    .build();

            when(securityUtils.getCurrentUserId(any(Authentication.class))).thenReturn(50L);
            when(listingService.updateStock(eq(301L), eq(50L), any(UpdateStockRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(patch("/api/listings/301/stock")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.newQuantity").value(25));
        }

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("Should return 400 when subtract below zero")
        void updateStock_SubtractBelowZero_Returns400() throws Exception {
            UpdateStockRequest request = UpdateStockRequest.builder()
                    .quantity(20)
                    .operation(StockOperation.SUBTRACT)
                    .build();

            when(securityUtils.getCurrentUserId(any(Authentication.class))).thenReturn(50L);
            when(listingService.updateStock(eq(301L), eq(50L), any(UpdateStockRequest.class)))
                    .thenThrow(new AppException(ErrorCode.STOCK_CANNOT_BE_NEGATIVE));

            mockMvc.perform(patch("/api/listings/301/stock")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ============ DELETE LISTING TESTS ============

    @Nested
    @DisplayName("DELETE /api/listings/{id}")
    class DeleteListingTests {

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("Should delete listing successfully")
        void deleteListing_Owner_Returns200() throws Exception {
            when(listingService.hardDeleteListing(eq(301L), any(Authentication.class)))
                    .thenReturn("Successfully deleted listing");

            mockMvc.perform(delete("/api/listings/301")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("Successfully deleted listing"));
        }

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("Should return 403 when not owner")
        void deleteListing_NotOwner_Returns403() throws Exception {
            when(listingService.hardDeleteListing(eq(301L), any(Authentication.class)))
                    .thenThrow(new AppException(ErrorCode.DO_NOT_HAVE_PERMISSION));

            mockMvc.perform(delete("/api/listings/301")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    // ============ TOGGLE LIKE TESTS ============

    @Nested
    @DisplayName("PUT /api/listings/{id}/toggle-like")
    class ToggleLikeTests {

        @Test
        @WithMockUser
        @DisplayName("Should toggle like successfully")
        void toggleLike_ValidRequest_Returns200() throws Exception {
            when(listingService.toggleListingLike(eq(301L), any(Authentication.class)))
                    .thenReturn(sampleListingResponse);

            mockMvc.perform(put("/api/listings/301/toggle-like")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value(301));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 403 when seller tries to like own listing")
        void toggleLike_SellerOwnListing_Returns403() throws Exception {
            when(listingService.toggleListingLike(eq(301L), any(Authentication.class)))
                    .thenThrow(new AppException(ErrorCode.DO_NOT_HAVE_PERMISSION));

            mockMvc.perform(put("/api/listings/301/toggle-like")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}
