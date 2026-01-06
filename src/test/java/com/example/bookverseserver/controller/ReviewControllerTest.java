package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Review.CreateReviewRequest;
import com.example.bookverseserver.dto.response.Review.HelpfulVoteResponse;
import com.example.bookverseserver.dto.response.Review.ReviewResponse;
import com.example.bookverseserver.dto.response.Review.ReviewStatsResponse;
import com.example.bookverseserver.dto.response.Review.ReviewsListResponse;
import com.example.bookverseserver.service.AuthenticationService;
import com.example.bookverseserver.service.ReviewService;
import com.example.bookverseserver.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ReviewService reviewService;

        @MockBean
        private SecurityUtils securityUtils;

        @MockBean
        private AuthenticationService authenticationService;

        private final Long TEST_USER_ID = 1L;
        private final Long TEST_BOOK_ID = 1L;
        private final Long TEST_REVIEW_ID = 1L;

        @Test
        @WithMockUser
        void createReview_ValidRequest_Returns201() throws Exception {
                // Arrange
                when(securityUtils.getCurrentUserId(any(Authentication.class))).thenReturn(TEST_USER_ID);

                CreateReviewRequest request = CreateReviewRequest.builder()
                                .rating(5)
                                .comment("Great book!")
                                .build();

                ReviewResponse response = ReviewResponse.builder()
                                .id(1L)
                                .bookId(TEST_BOOK_ID)
                                .userId(TEST_USER_ID)
                                .rating(5)
                                .comment("Great book!")
                                .build();

                when(reviewService.createReview(eq(TEST_BOOK_ID), any(CreateReviewRequest.class), eq(TEST_USER_ID)))
                                .thenReturn(response);

                // Act & Assert
                mockMvc.perform(post("/books/{bookId}/reviews", TEST_BOOK_ID)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.result.rating", is(5)))
                                .andExpect(jsonPath("$.result.comment", is("Great book!")));
        }

        @Test
        void createReview_Unauthorized_RedirectsToOAuth2() throws Exception {
                // Arrange
                CreateReviewRequest request = CreateReviewRequest.builder()
                                .rating(5)
                                .comment("Great book!")
                                .build();

                // Act & Assert - No @WithMockUser, expects redirect to OAuth2 login
                mockMvc.perform(post("/books/{bookId}/reviews", TEST_BOOK_ID)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrlPattern("**/oauth2/authorization/google"));
        }

        @Test
        @WithMockUser
        void listReviews_WithPagination_ReturnsCorrectPage() throws Exception {
                // Arrange
                ReviewsListResponse response = ReviewsListResponse.builder()
                                .reviews(List.of())
                                .stats(ReviewStatsResponse.builder()
                                                .bookId(TEST_BOOK_ID)
                                                .averageRating(4.5)
                                                .totalReviews(10)
                                                .ratingDistribution(Map.of(5, 5, 4, 3, 3, 2, 2, 0, 1, 0))
                                                .build())
                                .meta(ReviewsListResponse.PaginationMeta.builder()
                                                .page(0)
                                                .totalPages(2)
                                                .totalItems(20L)
                                                .itemsPerPage(10)
                                                .build())
                                .build();

                when(reviewService.getReviewsByBookId(eq(TEST_BOOK_ID), eq(0), eq(10),
                                anyString(), anyString(), isNull(), any()))
                                .thenReturn(response);

                // Act & Assert
                mockMvc.perform(get("/books/{bookId}/reviews", TEST_BOOK_ID)
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result.meta.page", is(0)))
                                .andExpect(jsonPath("$.result.meta.totalPages", is(2)))
                                .andExpect(jsonPath("$.result.stats.averageRating", is(4.5)));
        }

        @Test
        @WithMockUser
        void getBookRating_ReturnsStats() throws Exception {
                // Arrange
                ReviewStatsResponse response = ReviewStatsResponse.builder()
                                .bookId(TEST_BOOK_ID)
                                .averageRating(4.2)
                                .totalReviews(50)
                                .ratingDistribution(Map.of(5, 20, 4, 15, 3, 10, 2, 3, 1, 2))
                                .build();

                when(reviewService.getBookRating(TEST_BOOK_ID)).thenReturn(response);

                // Act & Assert
                mockMvc.perform(get("/books/{bookId}/rating", TEST_BOOK_ID))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result.averageRating", is(4.2)))
                                .andExpect(jsonPath("$.result.totalReviews", is(50)));
        }

        @Test
        @WithMockUser
        void toggleHelpful_Returns200() throws Exception {
                // Arrange
                when(securityUtils.getCurrentUserId(any(Authentication.class))).thenReturn(TEST_USER_ID);

                HelpfulVoteResponse response = HelpfulVoteResponse.builder()
                                .reviewId(TEST_REVIEW_ID)
                                .helpfulCount(5)
                                .userHasVoted(true)
                                .build();

                when(reviewService.toggleHelpful(TEST_REVIEW_ID, TEST_USER_ID)).thenReturn(response);

                // Act & Assert
                mockMvc.perform(post("/reviews/{id}/helpful", TEST_REVIEW_ID)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result.helpfulCount", is(5)))
                                .andExpect(jsonPath("$.result.userHasVoted", is(true)));
        }

        @Test
        @WithMockUser
        void deleteReview_Success_Returns204() throws Exception {
                // Arrange
                when(securityUtils.getCurrentUserId(any(Authentication.class))).thenReturn(TEST_USER_ID);

                // Act & Assert
                mockMvc.perform(delete("/reviews/{id}", TEST_REVIEW_ID)
                                .with(csrf()))
                                .andExpect(status().isNoContent());
        }
}
