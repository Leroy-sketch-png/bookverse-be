package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Book.IsbnRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
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

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
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
            
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Items per page", example = "20")
            @RequestParam(defaultValue = "20") int limit) {
        return bookService.getBooks(q, author_id, category_id, page, limit);
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