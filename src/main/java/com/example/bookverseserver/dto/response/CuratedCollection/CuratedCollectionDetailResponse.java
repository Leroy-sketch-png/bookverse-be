package com.example.bookverseserver.dto.response.CuratedCollection;

import com.example.bookverseserver.dto.response.Book.BookResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Curated collection detail view with all books")
public class CuratedCollectionDetailResponse {

    @Schema(description = "Display name of the collection")
    private String name;

    @Schema(description = "URL-friendly identifier")
    private String slug;

    @Schema(description = "Brief description of the collection")
    private String description;

    @Schema(description = "Total number of books in this collection")
    private Integer totalBooks;

    @Schema(description = "Array of 3 random cover image URLs from books in this collection")
    private List<String> coverImageUrls;

    @Schema(description = "All books in this collection (no pagination)")
    private List<BookResponse> books;
}
