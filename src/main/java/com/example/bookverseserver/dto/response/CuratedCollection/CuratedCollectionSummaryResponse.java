package com.example.bookverseserver.dto.response.CuratedCollection;

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
@Schema(description = "Curated collection summary for list view")
public class CuratedCollectionSummaryResponse {

    @Schema(description = "Unique identifier")
    private Long id;

    @Schema(description = "URL-friendly identifier")
    private String slug;

    @Schema(description = "Display name of the collection")
    private String name;

    @Schema(description = "Brief description of the collection")
    private String description;

    @Schema(description = "Array of 3 random cover image URLs from books in this collection")
    private List<String> coverImageUrls;
}
