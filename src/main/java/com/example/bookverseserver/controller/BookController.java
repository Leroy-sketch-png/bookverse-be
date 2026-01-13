package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Book.IsbnRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.service.BookEnrichmentService;
import com.example.bookverseserver.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "📖 Book catalog and metadata management APIs - Search books, view details, import from OpenLibrary")
public class BookController {

    private final BookService bookService;
    private final BookEnrichmentService bookEnrichmentService;

    @Autowired
    public BookController(BookService bookService, BookEnrichmentService bookEnrichmentService) {
        this.bookService = bookService;
        this.bookEnrichmentService = bookEnrichmentService;
    }

    @GetMapping
    @Operation(
        summary = "Search and filter books",
        description = "Get paginated list of books with optional search and filters. " +
                     "Supports: " +
                     "- **Full-text search** (title, author name, ISBN) " +
                     "- **Author filter** by author ID " +
                     "- **Category filter** by category ID " +
                     "Results include book metadata, authors, average rating, and available listings."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Books retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid query parameters"
        )
    })
    public ApiResponse<?> getAllBooks(
            @Parameter(description = "Search query (title, author, ISBN)", example = "Clean Code")
            @RequestParam(required = false) String q,
            
            @Parameter(description = "Filter by author ID", example = "1")
            @RequestParam(required = false) String author_id,
            
            @Parameter(description = "Filter by category ID", example = "1")
            @RequestParam(required = false) String category_id,
            
            @Parameter(description = "Page number (1-indexed)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "Items per page", example = "20")
            @RequestParam(defaultValue = "20") int limit) {
        // Convert 1-indexed (API) to 0-indexed (Spring)
        int pageIndex = Math.max(0, page - 1);
        return bookService.getBooks(q, author_id, category_id, pageIndex, limit);
    }

    @GetMapping("/{bookId}")
    @Operation(
        summary = "Get book details by ID",
        description = "Retrieve comprehensive book information including: " +
                     "- Full metadata (title, ISBN, publisher, description) " +
                     "- Author details with biographies " +
                     "- Categories and tags " +
                     "- Average rating and review count " +
                     "- Available listings from different sellers " +
                     "- Price range and conditions available"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Book found successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Book not found"
        )
    })
    public ApiResponse<?> getBookById(
        @Parameter(description = "Book ID", example = "1", required = true)
        @PathVariable String bookId
    ) {
        return bookService.getBookById(bookId);
    }

    @GetMapping("/lookup/{isbn}")
    @Operation(
        summary = "Preview book data by ISBN (no database write)",
        description = "Fetches enriched book data from multiple external sources (OpenLibrary + Google Books) " +
                     "WITHOUT creating the book in the database. " +
                     "**Perfect for**: " +
                     "- ISBN auto-fill preview in seller listing form " +
                     "- Validating ISBN before creating a listing " +
                     "- Previewing book metadata before import " +
                     "**Features**: " +
                     "- Multi-source enrichment (best data from OpenLibrary + Google Books) " +
                     "- Normalized categories mapped to our canonical 10 categories " +
                     "- Quality score (0-100) indicating data completeness " +
                     "**Supports both ISBN-10 and ISBN-13**."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Book data retrieved (may be empty if not found)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid ISBN format"
        )
    })
    public ApiResponse<BookEnrichmentService.EnrichedBookResult> lookupBookByIsbn(
        @Parameter(description = "ISBN-10 or ISBN-13 (hyphens allowed)", example = "978-0134685991", required = true)
        @PathVariable String isbn
    ) {
        BookEnrichmentService.EnrichedBookResult result = bookEnrichmentService.fetchEnrichedBookData(isbn);
        return ApiResponse.<BookEnrichmentService.EnrichedBookResult>builder()
                .result(result)
                .message(result.isFound() ? "Book found" : "Book not found in external sources")
                .build();
    }

    @PostMapping("/from-open-library")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Import book from OpenLibrary",
        description = "Create a new book by importing metadata from OpenLibrary using ISBN. " +
                     "**Automatically fetches**: " +
                     "- Title and subtitle " +
                     "- Author information " +
                     "- Cover images " +
                     "- Publisher and publish date " +
                     "- Description and subjects " +
                     "**Supports both ISBN-10 and ISBN-13**. " +
                     "Example ISBNs: 9780134685991 (Effective Java), 9780132350884 (Clean Code)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Book created successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid ISBN format"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Book not found in OpenLibrary"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", 
            description = "Book already exists in database"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "OpenLibrary API error"
        )
    })
    public ApiResponse<String> createBookFromOpenLibrary(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "ISBN number (10 or 13 digits)",
            required = true,
            content = @Content(schema = @Schema(
                implementation = IsbnRequest.class,
                example = "{\"isbn\": \"9780134685991\"}"
            ))
        )
        @RequestBody IsbnRequest request
    ) {
        String bookId = bookService.createBookFromOpenLibrary(request.getIsbn());
        return ApiResponse.<String>builder().message("Book created successfully").result(bookId).build();
    }
}