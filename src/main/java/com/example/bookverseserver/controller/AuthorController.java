package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Book.AuthorDetailRequest;
import com.example.bookverseserver.dto.request.Book.AuthorRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Book.AuthorDetailResponse;
import com.example.bookverseserver.dto.response.Book.AuthorResponse;
import com.example.bookverseserver.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/author")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Authors", description = "📚 Author management and information APIs - Search authors, view biographies, manage author data")
public class AuthorController {
    AuthorService authorService;

    @GetMapping("/ol/{olid}")
    @Operation(
        summary = "Get author by OpenLibrary ID",
        description = "Retrieve detailed author information including biography, birth date, and list of books. " +
                     "Fetches from OpenLibrary API if not cached locally. " +
                     "**Example OpenLibrary IDs**: OL23919A (Joshua Bloch), OL26320A (Robert C. Martin)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Author found successfully",
            content = @Content(schema = @Schema(implementation = AuthorDetailResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Author not found in database or OpenLibrary"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "OpenLibrary API error"
        )
    })
    ApiResponse<AuthorDetailResponse> getAuthorByOLID(
        @Parameter(description = "OpenLibrary Author ID (format: OL23919A)", example = "OL23919A", required = true)
        @PathVariable("olid") String olid
    ) {
        return ApiResponse.<AuthorDetailResponse>builder()
                .result(authorService.getAuthorByOLID(olid))
                .build();
    }

    @GetMapping("/name/{name}")
    @Operation(
        summary = "Search authors by name",
        description = "Find authors by their name with partial matching. " +
                     "Case-insensitive search. Returns list of matching authors."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Authors found (may be empty list)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid name parameter"
        )
    })
    ApiResponse<List<AuthorResponse>> getAuthorsByName(
        @Parameter(description = "Author name to search (partial match supported)", example = "Martin", required = true)
        @PathVariable("name") String name
    ) {
        return ApiResponse.<List<AuthorResponse>>builder()
                .result(authorService.getAuthorsByName(name))
                .build();
    }

    @GetMapping
    @Operation(
        summary = "Get all authors",
        description = "Retrieve list of all authors in the database. " +
                     "**Note**: This endpoint may return large data. Consider adding pagination in the future."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Authors retrieved successfully"
        )
    })
    ApiResponse<List<AuthorResponse>> getAllAuthors() {
        return ApiResponse.<List<AuthorResponse>>builder()
                .result(authorService.getAllAuthors())
                .build();
    }

    @GetMapping("/national/{national}")
    @Operation(
        summary = "Filter authors by nationality",
        description = "Get authors filtered by their nationality/country. " +
                     "**Examples**: 'American', 'British', 'Japanese', 'French'"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Authors retrieved successfully"
        )
    })
    ApiResponse<List<AuthorResponse>> getAllAuthorsByNationality(
        @Parameter(description = "Nationality filter", example = "American", required = true)
        @PathVariable("national") String national
    ) {
        return ApiResponse.<List<AuthorResponse>>builder()
                .result(authorService.getAllAuthorsByNationality(national))
                .build();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get author by ID",
        description = "Retrieve a single author by their database ID. " +
                     "Returns full author details including bio and works."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Author found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Author not found"
        )
    })
    ApiResponse<AuthorResponse> getAuthorById(
        @Parameter(description = "Author ID", example = "1", required = true)
        @PathVariable("id") Long id
    ) {
        return ApiResponse.<AuthorResponse>builder()
                .result(authorService.getAuthorById(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Create new author (Admin only)",
        description = "Add a new author to the database manually. **Requires ADMIN role**. " +
                     "Use this when importing authors not available in OpenLibrary."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Author created successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid author data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Forbidden - Admin access required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", 
            description = "Author already exists"
        )
    })
    ApiResponse<AuthorResponse> createAuthor(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Author details including name, biography, birth date",
            required = true,
            content = @Content(schema = @Schema(implementation = AuthorRequest.class))
        )
        @Valid @RequestBody AuthorRequest authorRequest
    ) {
        return ApiResponse.<AuthorResponse>builder()
                .result(authorService.addAuthor(authorRequest))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Update author information (Admin only)",
        description = "Update existing author details. **Requires ADMIN role**. " +
                     "Supports updating biography, nationality, birth/death dates."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Author updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid update data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Forbidden - Admin access required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Author not found"
        )
    })
    ApiResponse<AuthorDetailResponse> updateAuthor(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated author details",
            required = true,
            content = @Content(schema = @Schema(implementation = AuthorDetailRequest.class))
        )
        @Valid @RequestBody AuthorDetailRequest authorRequest,
        
        @Parameter(description = "OpenLibrary Author ID", example = "OL23919A", required = true)
        @PathVariable("id") String id
    ) {
        return ApiResponse.<AuthorDetailResponse>builder()
                .result(authorService.updateAuthor(id, authorRequest))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Delete author (Admin only)",
        description = "Remove author from database. **Requires ADMIN role**. " +
                     "**Warning**: This may affect books associated with this author."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Author deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Forbidden - Admin access required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Author not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", 
            description = "Cannot delete - author has associated books"
        )
    })
    ApiResponse<AuthorDetailResponse> deleteAuthor(
        @Parameter(description = "OpenLibrary Author ID to delete", example = "OL23919A", required = true)
        @PathVariable("id") String id
    ) {
        return ApiResponse.<AuthorDetailResponse>builder()
                .result(authorService.deleteAuthor(id))
                .build();
    }
}
