package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.CuratedCollection.CuratedCollectionDetailResponse;
import com.example.bookverseserver.dto.response.CuratedCollection.CuratedCollectionSummaryResponse;
import com.example.bookverseserver.service.CuratedCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/curated-collections")
@RequiredArgsConstructor
@Tag(name = "Curated Collections", description = "📚 Browse curated book collections by theme - Discover page collections like 'Must-read for Developers', 'AI & The Future'")
public class CuratedCollectionController {

    private final CuratedCollectionService curatedCollectionService;

    @GetMapping
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Curated collections retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CuratedCollectionSummaryResponse.class))))
    })
    public ApiResponse<List<CuratedCollectionSummaryResponse>> getAllCollections() {
        return curatedCollectionService.getAllCollections();
    }

    @GetMapping("/{slug}")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Curated collection found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CuratedCollectionDetailResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Curated collection not found")
    })
    public ApiResponse<CuratedCollectionDetailResponse> getCollectionBySlug(
            @Parameter(description = "URL-friendly identifier of the collection", example = "dev-must-read", required = true) @PathVariable String slug) {
        return curatedCollectionService.getCollectionBySlug(slug);
    }
}
