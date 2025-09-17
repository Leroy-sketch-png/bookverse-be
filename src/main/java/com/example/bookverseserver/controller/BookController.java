package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Book.IsbnRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ApiResponse<?> getAllBooks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String author_id,
            @RequestParam(required = false) String category_id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return bookService.getBooks(q, author_id, category_id, page, limit);
    }

    @GetMapping("/{bookId}")
    public ApiResponse<?> getBookById(@PathVariable String bookId) {
        return bookService.getBookById(bookId);
    }

    @PostMapping("/from-open-library")
    public ApiResponse<String> createBookFromOpenLibrary(@RequestBody IsbnRequest request) {
        String bookId = bookService.createBookFromOpenLibrary(request.getIsbn());
        return ApiResponse.<String>builder().message("Book created successfully").result(bookId).build();
    }
}