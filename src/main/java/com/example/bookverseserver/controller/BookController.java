package com.example.bookverseserver.controller;

import java.util.List;

import com.example.bookverseserver.dto.request.Book.BookRequest;
import com.example.bookverseserver.service.BookService;
import org.springframework.web.bind.annotation.*;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Book.BookResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BookController {
    BookService bookService;

    @PostMapping
    ApiResponse<BookResponse> createBook(@RequestBody BookRequest bookRequest) {
        return ApiResponse.<BookResponse>builder()
                .result(bookService.createBook(bookRequest))
                .build();
    }

    @GetMapping
    ApiResponse<List<BookResponse>> getAllBooks() {
        return ApiResponse.<List<BookResponse>>builder()
                .result(bookService.getAllBooks())
                .build();
    }

    @GetMapping("/category/{categoryId}")
    ApiResponse<List<BookResponse>> getBooksByCategoryId(@PathVariable Long categoryId) {
        return ApiResponse.<List<BookResponse>>builder()
                .result(bookService.getAllBooksByCategory(categoryId))
                .build();
    }

    @GetMapping("/category/name/{categoryName}")
    ApiResponse<List<BookResponse>> getBooksByCategoryName(@PathVariable("categoryName") String categoryName) {
        return ApiResponse.<List<BookResponse>>builder()
                .result(bookService.getAllBooksByCategoryName(categoryName))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<BookResponse> getBookById(@PathVariable("id") Long id) {
        return ApiResponse.<BookResponse>builder()
                .result(bookService.getBookById(id))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<BookResponse> updateBookById(@RequestBody BookRequest bookRequest,@PathVariable("id") Long id) {
        return ApiResponse.<BookResponse>builder()
                .result(bookService.updateBook(id, bookRequest))
                .build();
    }

}
