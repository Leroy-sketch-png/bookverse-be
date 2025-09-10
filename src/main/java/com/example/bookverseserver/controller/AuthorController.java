package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Book.AuthorDetailRequest;
import com.example.bookverseserver.dto.request.Book.AuthorRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Book.AuthorDetailResponse;
import com.example.bookverseserver.dto.response.Book.AuthorResponse;
import com.example.bookverseserver.service.AuthorService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/author")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorController {
    AuthorService authorService;

    @GetMapping({"/{id}"})
    ApiResponse<AuthorDetailResponse> getAuthorByOLID(@PathVariable("id") String id) {
        return ApiResponse.<AuthorDetailResponse>builder()
                .result(authorService.getAuthorByOLID(id))
                .build();
    }

    @GetMapping("/name/{name}")
    ApiResponse<List<AuthorResponse>> getAuthorsByName(@PathVariable("name") String name) {
        return ApiResponse.<List<AuthorResponse>>builder()
                .result(authorService.getAuthorsByName(name))
                .build();
    }

    @GetMapping
    ApiResponse<List<AuthorResponse>> getAllAuthors() {
        return ApiResponse.<List<AuthorResponse>>builder()
                .result(authorService.getAllAuthors())
                .build();
    }

    @GetMapping("/national/{national}")
    ApiResponse<List<AuthorResponse>> getAllAuthorsByNationality(@PathVariable("national") String national) {
        return ApiResponse.<List<AuthorResponse>>builder()
                .result(authorService.getAllAuthorsByNationality(national))
                .build();
    }
//
//    @GetMapping("/name/{name}")
//    ApiResponse<List<AuthorResponse>> getAllAuthorsByName(@PathVariable("name") String name) {
//        return ApiResponse.<List<AuthorResponse>>builder()
//                .result(authorService.getAllAuthorsByName(name))
//                .build();
//    }

//    @GetMapping("/{id}")
//    ApiResponse<AuthorResponse> getAuthorById(@PathVariable("id") Long id) {
//        return ApiResponse.<AuthorResponse>builder()
//                .result(authorService.getAuthorById(id))
//                .build();
//    }

    @PostMapping
    ApiResponse<AuthorResponse> createAuthor(@RequestBody AuthorRequest authorRequest) {
        return ApiResponse.<AuthorResponse>builder()
                .result(authorService.addAuthor(authorRequest))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<AuthorDetailResponse> updateAuthor(@RequestBody AuthorDetailRequest authorRequest, @PathVariable("id") String id) {
        return ApiResponse.<AuthorDetailResponse>builder()
                .result(authorService.updateAuthor(id, authorRequest))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<AuthorDetailResponse> deleteAuthor(@PathVariable("id") String id) {
        return ApiResponse.<AuthorDetailResponse>builder()
                .result(authorService.deleteAuthor(id))
                .build();
    }
}
