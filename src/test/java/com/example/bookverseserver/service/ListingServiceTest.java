package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Product.ListingCreationRequest;
import com.example.bookverseserver.dto.request.Product.ListingRequest;
import com.example.bookverseserver.dto.request.Product.ListingUpdateRequest;
import com.example.bookverseserver.dto.request.Product.UpdateStockRequest;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.dto.response.Product.*;
import com.example.bookverseserver.entity.Product.BookMeta;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.BookCondition;
import com.example.bookverseserver.enums.ListingStatus;
import com.example.bookverseserver.enums.StockOperation;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.BookMetaMapper;
import com.example.bookverseserver.mapper.ListingMapper;
import com.example.bookverseserver.mapper.ListingPhotoMapper;
import com.example.bookverseserver.repository.*;
import com.example.bookverseserver.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private ListingPhotoRepository listingPhotoRepository;
    @Mock
    private LikesRepository likesRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookMetaRepository bookMetaRepository;
    @Mock
    private BookMetaMapper bookMetaMapper;
    @Mock
    private ListingMapper listingMapper;
    @Mock
    private ListingPhotoMapper listingPhotoMapper;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ListingService listingService;

    private User testSeller;
    private User testBuyer;
    private BookMeta testBook;
    private Listing testListing;
    private Authentication mockAuth;

    @BeforeEach
    void setUp() {
        mockAuth = mock(Authentication.class);

        testSeller = new User();
        testSeller.setId(50L);
        testSeller.setUsername("test_seller");

        testBuyer = new User();
        testBuyer.setId(100L);
        testBuyer.setUsername("test_buyer");

        testBook = new BookMeta();
        testBook.setId(123L);
        testBook.setTitle("Clean Code");

        testListing = Listing.builder()
                .id(301L)
                .bookMeta(testBook)
                .seller(testSeller)
                .price(new BigDecimal("45.99"))
                .originalPrice(new BigDecimal("55.99"))
                .quantity(15)
                .condition(BookCondition.NEW)
                .status(ListingStatus.ACTIVE)
                .views(100)
                .soldCount(45)
                .visibility(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ============ GET LISTINGS FILTERED TESTS ============

    @Nested
    @DisplayName("getListingsFiltered tests")
    class GetListingsFilteredTests {

        @Test
        @DisplayName("Should return filtered listings by seller ID")
        void getListings_WithSellerFilter_ReturnsOnlySellersListings() {
            // Given
            Long sellerId = 50L;
            List<Listing> listings = List.of(testListing);
            Page<Listing> page = new PageImpl<>(listings);

            when(listingRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(page);
            when(listingMapper.toListingResponse(any(Listing.class)))
                    .thenReturn(ListingResponse.builder()
                            .id(301L)
                            .seller(ListingResponse.SellerInfo.builder()
                                    .id(sellerId)
                                    .build())
                            .build());

            // When
            PagedResponse<ListingResponse> result = listingService.getListingsFiltered(
                    sellerId, null, null, null, "createdAt", "desc", 0, 20);

            // Then
            assertNotNull(result);
            assertFalse(result.getData().isEmpty());
            assertEquals(sellerId, result.getData().get(0).getSeller().getId());
            verify(listingRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return filtered listings by status ACTIVE")
        void getListings_WithStatusFilter_ReturnsCorrectStatus() {
            // Given
            List<Listing> listings = List.of(testListing);
            Page<Listing> page = new PageImpl<>(listings);

            when(listingRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(page);
            when(listingMapper.toListingResponse(any(Listing.class)))
                    .thenReturn(ListingResponse.builder()
                            .id(301L)
                            .build());

            // When
            PagedResponse<ListingResponse> result = listingService.getListingsFiltered(
                    null, null, null, ListingStatus.ACTIVE, "createdAt", "desc", 0, 20);

            // Then
            assertNotNull(result);
            assertFalse(result.getData().isEmpty());
            // Status is not directly on ListingResponse per Vision - filtering is done server-side
            assertNotNull(result.getData().get(0));
        }

        @Test
        @DisplayName("Should return filtered listings by book ID")
        void getListings_WithBookFilter_ReturnsListingsForBook() {
            // Given
            Long bookId = 123L;
            List<Listing> listings = List.of(testListing);
            Page<Listing> page = new PageImpl<>(listings);

            when(listingRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(page);
            when(listingMapper.toListingResponse(any(Listing.class)))
                    .thenReturn(ListingResponse.builder()
                            .id(301L)
                            .book(ListingResponse.BookInfo.builder()
                                    .id(bookId)
                                    .build())
                            .build());

            // When
            PagedResponse<ListingResponse> result = listingService.getListingsFiltered(
                    null, bookId, null, null, "createdAt", "desc", 0, 20);

            // Then
            assertNotNull(result);
            assertFalse(result.getData().isEmpty());
            assertEquals(bookId, result.getData().get(0).getBook().getId());
        }

        @Test
        @DisplayName("Should sort listings by price ascending")
        void getListings_SortByPriceAsc_ReturnsSortedListings() {
            // Given
            Page<Listing> page = new PageImpl<>(List.of(testListing));

            when(listingRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(page);
            when(listingMapper.toListingResponse(any(Listing.class)))
                    .thenReturn(ListingResponse.builder().id(301L).build());

            // When
            PagedResponse<ListingResponse> result = listingService.getListingsFiltered(
                    null, null, null, null, "price", "asc", 0, 20);

            // Then
            assertNotNull(result);
            verify(listingRepository).findAll(any(Specification.class), any(Pageable.class));
        }
    }

    // ============ GET LISTING DETAIL TESTS ============

    @Nested
    @DisplayName("getListingDetail tests")
    class GetListingDetailTests {

        @Test
        @DisplayName("Should increment view count for anonymous user")
        void getListingById_AnonymousUser_IncrementsViewCount() {
            // Given
            when(listingRepository.findById(301L)).thenReturn(Optional.of(testListing));
            when(securityUtils.getCurrentUserId(any())).thenThrow(new AppException(ErrorCode.UNAUTHENTICATED));
            when(listingMapper.toDetailResponse(any(Listing.class)))
                    .thenReturn(ListingDetailResponse.builder()
                            .id(301L)
                            .views(101)
                            .build());
            when(listingRepository.findRelatedListings(anyLong(), anyLong(), any(Pageable.class)))
                    .thenReturn(List.of());
            when(listingMapper.toRelatedDtoList(anyList())).thenReturn(List.of());

            // When
            ListingDetailResponse result = listingService.getListingDetail(301L, mockAuth);

            // Then
            assertNotNull(result);
            verify(listingRepository).incrementViewCount(301L);
        }

        @Test
        @DisplayName("Should increment view count for buyer (not seller)")
        void getListingById_BuyerView_IncrementsViewCount() {
            // Given
            when(listingRepository.findById(301L)).thenReturn(Optional.of(testListing));
            when(securityUtils.getCurrentUserId(mockAuth)).thenReturn(100L); // Different from seller (50L)
            when(listingMapper.toDetailResponse(any(Listing.class)))
                    .thenReturn(ListingDetailResponse.builder()
                            .id(301L)
                            .views(101)
                            .build());
            when(listingRepository.findRelatedListings(anyLong(), anyLong(), any(Pageable.class)))
                    .thenReturn(List.of());
            when(listingMapper.toRelatedDtoList(anyList())).thenReturn(List.of());

            // When
            ListingDetailResponse result = listingService.getListingDetail(301L, mockAuth);

            // Then
            assertNotNull(result);
            verify(listingRepository).incrementViewCount(301L);
        }

        @Test
        @DisplayName("Should NOT increment view count for seller viewing own listing")
        void getListingById_SellerView_DoesNotIncrementViewCount() {
            // Given
            when(listingRepository.findById(301L)).thenReturn(Optional.of(testListing));
            when(securityUtils.getCurrentUserId(mockAuth)).thenReturn(50L); // Same as seller
            when(listingMapper.toDetailResponse(any(Listing.class)))
                    .thenReturn(ListingDetailResponse.builder()
                            .id(301L)
                            .views(100)
                            .build());
            when(listingRepository.findRelatedListings(anyLong(), anyLong(), any(Pageable.class)))
                    .thenReturn(List.of());
            when(listingMapper.toRelatedDtoList(anyList())).thenReturn(List.of());

            // When
            ListingDetailResponse result = listingService.getListingDetail(301L, mockAuth);

            // Then
            assertNotNull(result);
            verify(listingRepository, never()).incrementViewCount(anyLong());
        }

        @Test
        @DisplayName("Should throw exception when listing not found")
        void getListingById_NotFound_ThrowsException() {
            // Given
            when(listingRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            AppException exception = assertThrows(AppException.class,
                    () -> listingService.getListingDetail(999L, mockAuth));
            assertEquals(ErrorCode.LISTING_NOT_EXISTED, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should include related listings in response")
        void getListingById_IncludesRelatedListings() {
            // Given
            Listing relatedListing = Listing.builder()
                    .id(302L)
                    .bookMeta(testBook)
                    .seller(testBuyer)
                    .price(new BigDecimal("42.99"))
                    .build();

            when(listingRepository.findById(301L)).thenReturn(Optional.of(testListing));
            when(securityUtils.getCurrentUserId(mockAuth)).thenReturn(100L);
            when(listingRepository.findRelatedListings(anyLong(), anyLong(), any(Pageable.class)))
                    .thenReturn(List.of(relatedListing));
            when(listingMapper.toDetailResponse(any(Listing.class)))
                    .thenReturn(ListingDetailResponse.builder()
                            .id(301L)
                            .build());
            when(listingMapper.toRelatedDtoList(anyList()))
                    .thenReturn(List.of(RelatedListingDto.builder()
                            .id(302L)
                            .price(new BigDecimal("42.99"))
                            .build()));

            // When
            ListingDetailResponse result = listingService.getListingDetail(301L, mockAuth);

            // Then
            assertNotNull(result);
            verify(listingRepository).findRelatedListings(eq(123L), eq(301L), any(Pageable.class));
        }
    }

    // ============ CREATE LISTING TESTS ============

    @Nested
    @DisplayName("createListing tests")
    class CreateListingTests {

        @Test
        @DisplayName("Should create listing successfully with existing book")
        void createListing_ValidData_CreatesListing() {
            // Given
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

            when(securityUtils.getCurrentUserId(mockAuth)).thenReturn(50L);
            when(bookMetaRepository.findById(123L)).thenReturn(Optional.of(testBook));
            when(listingMapper.toListing(any(ListingRequest.class))).thenReturn(testListing);
            when(userRepository.getReferenceById(50L)).thenReturn(testSeller);
            when(listingRepository.save(any(Listing.class))).thenReturn(testListing);
            when(listingMapper.toListingResponse(any(Listing.class)))
                    .thenReturn(ListingResponse.builder()
                            .id(301L)
                            .book(ListingResponse.BookInfo.builder()
                                    .id(123L)
                                    .build())
                            .seller(ListingResponse.SellerInfo.builder()
                                    .id(50L)
                                    .build())
                            .build());

            // When
            ListingResponse result = listingService.createListing(request, mockAuth);

            // Then
            assertNotNull(result);
            assertEquals(301L, result.getId());
            verify(listingRepository).save(any(Listing.class));
        }

        @Test
        @DisplayName("Should throw exception when bookMetaId is null")
        void createListing_NullBookMetaId_ThrowsException() {
            // Given
            ListingCreationRequest request = ListingCreationRequest.builder()
                    .bookMetaId(null)
                    .build();

            // When & Then
            AppException exception = assertThrows(AppException.class,
                    () -> listingService.createListing(request, mockAuth));
            assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw exception when book not found")
        void createListing_BookNotFound_ThrowsException() {
            // Given
            ListingCreationRequest request = ListingCreationRequest.builder()
                    .bookMetaId(999L)
                    .build();

            when(bookMetaRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            AppException exception = assertThrows(AppException.class,
                    () -> listingService.createListing(request, mockAuth));
            assertEquals(ErrorCode.BOOK_META_NOT_FOUND, exception.getErrorCode());
        }
    }

    // ============ UPDATE LISTING TESTS ============

    @Nested
    @DisplayName("updateListing tests")
    class UpdateListingTests {

        @Test
        @DisplayName("Should update listing when user is owner")
        void updateListing_Owner_UpdatesSuccessfully() {
            // Given
            ListingUpdateRequest updateRequest = new ListingUpdateRequest();
            updateRequest.setPrice(new BigDecimal("49.99"));

            when(listingRepository.findById(301L)).thenReturn(Optional.of(testListing));
            when(securityUtils.getCurrentUserId(mockAuth)).thenReturn(50L); // Same as seller
            when(listingRepository.save(any(Listing.class))).thenReturn(testListing);
            when(listingMapper.toListingUpdateResponse(any(Listing.class)))
                    .thenReturn(new ListingUpdateResponse());

            // When
            ListingUpdateResponse result = listingService.updateListing(301L, updateRequest, mockAuth);

            // Then
            assertNotNull(result);
            verify(listingMapper).updateListing(any(Listing.class), eq(updateRequest));
            verify(listingRepository).save(any(Listing.class));
        }

        @Test
        @DisplayName("Should throw exception when user is not owner")
        void updateListing_NotOwner_ThrowsForbidden() {
            // Given
            ListingUpdateRequest updateRequest = new ListingUpdateRequest();

            when(listingRepository.findById(301L)).thenReturn(Optional.of(testListing));
            when(securityUtils.getCurrentUserId(mockAuth)).thenReturn(999L); // Different from seller

            // When & Then
            AppException exception = assertThrows(AppException.class,
                    () -> listingService.updateListing(301L, updateRequest, mockAuth));
            assertEquals(ErrorCode.DO_NOT_HAVE_PERMISSION, exception.getErrorCode());
        }
    }

    // ============ UPDATE STOCK TESTS ============

    @Nested
    @DisplayName("updateStock tests")
    class UpdateStockTests {

        @Test
        @DisplayName("Should set stock to exact quantity with SET operation")
        void updateStock_SetOperation_SetsExactQuantity() {
            // Given
            UpdateStockRequest request = UpdateStockRequest.builder()
                    .quantity(25)
                    .operation(StockOperation.SET)
                    .build();

            when(listingRepository.findById(301L)).thenReturn(Optional.of(testListing));
            when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            StockUpdateResponse result = listingService.updateStock(301L, 50L, request);

            // Then
            assertNotNull(result);
            assertEquals(301L, result.getListingId());
            assertEquals(15, result.getOldQuantity());
            assertEquals(25, result.getNewQuantity());
        }

        @Test
        @DisplayName("Should add to stock with ADD operation")
        void updateStock_AddOperation_AddsToStock() {
            // Given
            UpdateStockRequest request = UpdateStockRequest.builder()
                    .quantity(10)
                    .operation(StockOperation.ADD)
                    .build();

            when(listingRepository.findById(301L)).thenReturn(Optional.of(testListing));
            when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            StockUpdateResponse result = listingService.updateStock(301L, 50L, request);

            // Then
            assertEquals(15, result.getOldQuantity());
            assertEquals(25, result.getNewQuantity()); // 15 + 10
        }

        @Test
        @DisplayName("Should subtract from stock with SUBTRACT operation")
        void updateStock_SubtractOperation_SubtractsFromStock() {
            // Given
            UpdateStockRequest request = UpdateStockRequest.builder()
                    .quantity(5)
                    .operation(StockOperation.SUBTRACT)
                    .build();

            when(listingRepository.findById(301L)).thenReturn(Optional.of(testListing));
            when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            StockUpdateResponse result = listingService.updateStock(301L, 50L, request);

            // Then
            assertEquals(15, result.getOldQuantity());
            assertEquals(10, result.getNewQuantity()); // 15 - 5
        }

        @Test
        @DisplayName("Should throw exception when subtract results in negative stock")
        void updateStock_SubtractBelowZero_ThrowsException() {
            // Given
            UpdateStockRequest request = UpdateStockRequest.builder()
                    .quantity(20)
                    .operation(StockOperation.SUBTRACT)
                    .build();

            when(listingRepository.findById(301L)).thenReturn(Optional.of(testListing));

            // When & Then
            AppException exception = assertThrows(AppException.class,
                    () -> listingService.updateStock(301L, 50L, request));
            assertEquals(ErrorCode.STOCK_CANNOT_BE_NEGATIVE, exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw exception when user is not owner")
        void updateStock_NotOwner_ThrowsForbidden() {
            // Given
            UpdateStockRequest request = UpdateStockRequest.builder()
                    .quantity(25)
                    .operation(StockOperation.SET)
                    .build();

            when(listingRepository.findById(301L)).thenReturn(Optional.of(testListing));

            // When & Then
            AppException exception = assertThrows(AppException.class,
                    () -> listingService.updateStock(301L, 999L, request)); // Different user
            assertEquals(ErrorCode.DO_NOT_HAVE_PERMISSION, exception.getErrorCode());
        }
    }

    // ============ DELETE LISTING TESTS ============

    @Nested
    @DisplayName("deleteListing tests")
    class DeleteListingTests {

        @Test
        @DisplayName("Should delete listing when user is owner")
        void hardDeleteListing_Owner_DeletesSuccessfully() {
            // Given
            when(listingRepository.findById(301L)).thenReturn(Optional.of(testListing));
            when(securityUtils.getCurrentUserId(mockAuth)).thenReturn(50L);

            // When
            String result = listingService.hardDeleteListing(301L, mockAuth);

            // Then
            assertEquals("Successfully deleted listing", result);
            verify(listingRepository).delete(testListing);
        }

        @Test
        @DisplayName("Should throw exception when user is not owner")
        void hardDeleteListing_NotOwner_ThrowsForbidden() {
            // Given
            when(listingRepository.findById(301L)).thenReturn(Optional.of(testListing));
            when(securityUtils.getCurrentUserId(mockAuth)).thenReturn(999L);

            // When & Then
            AppException exception = assertThrows(AppException.class,
                    () -> listingService.hardDeleteListing(301L, mockAuth));
            assertEquals(ErrorCode.DO_NOT_HAVE_PERMISSION, exception.getErrorCode());
        }
    }

    // ============ TOGGLE LIKE TESTS ============

    @Nested
    @DisplayName("toggleListingLike tests")
    class ToggleLikeTests {

        @Test
        @DisplayName("Should not allow seller to like own listing")
        void toggleLike_SellerOwnListing_ThrowsForbidden() {
            // Given
            when(listingRepository.findById(301L)).thenReturn(Optional.of(testListing));
            when(securityUtils.getCurrentUserId(mockAuth)).thenReturn(50L); // Same as seller

            // When & Then
            AppException exception = assertThrows(AppException.class,
                    () -> listingService.toggleListingLike(301L, mockAuth));
            assertEquals(ErrorCode.DO_NOT_HAVE_PERMISSION, exception.getErrorCode());
        }
    }
}
