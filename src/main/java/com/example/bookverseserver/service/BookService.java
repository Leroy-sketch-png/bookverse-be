//package com.example.bookverseserver.service;
//
//import com.example.bookverseserver.dto.request.Book.BookRequest;
//import com.example.bookverseserver.dto.response.Book.BookResponse;
//import com.example.bookverseserver.entity.Product.Book;
//import com.example.bookverseserver.exception.AppException;
//import com.example.bookverseserver.exception.ErrorCode;
//import com.example.bookverseserver.mapper.BookMapper;
//import com.example.bookverseserver.repository.BookRepository;
//import com.example.bookverseserver.service.CloudStorageService;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.AccessLevel;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//@Transactional
//public class BookService {
//
//    BookRepository bookRepository;
////    @Autowired
//    BookMapper bookMapper;
//    CloudStorageService cloudStorageService;
//
//    public List<BookResponse> getAllBooks() {
//        return bookRepository.findAll()
//                .stream()
//                .map(bookMapper::toBookResponse)
//                .toList();
//    }
//
//    public BookResponse getBookById(UUID id) {
//        Book book = (Book) bookRepository.findById(id)
//                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));
//        return bookMapper.toBookResponse(book);
//    }
//
//    @PreAuthorize("hasAuthority('SELLER')")
//    public BookResponse createBook(BookRequest request, Optional<MultipartFile> coverImage) {
//        String coverUrl = coverImage.map(cloudStorageService::uploadFile).orElse(null);
//        Book book = bookMapper.toBook(request);
//        book.setCoverImageUrl(coverUrl);
//        return bookMapper.toBookResponse(bookRepository.save(book));
//    }
//
//    @PreAuthorize("hasAuthority('ADMIN')")
//    public BookResponse updateBook(UUID id, BookRequest request, Optional<MultipartFile> coverImage) {
//        Book book = (Book) bookRepository.findById(id)
//                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));
//
//        bookMapper.updateBook(book, request);
//
//        coverImage.ifPresent(file -> {
//            String newCover = cloudStorageService.uploadFile(file);
//            cloudStorageService.deleteFile(book.getCoverImageUrl());
//            book.setCoverImageUrl(newCover);
//        });
//
//        return bookMapper.toBookResponse(bookRepository.save(book));
//    }
//
//    @PreAuthorize("hasAuthority('SELLER')")
//    public void deleteBook(UUID id) {
//        Book book = (Book) bookRepository.findById(id)
//                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));
//
//        if (book.getCoverImageUrl() != null) {
//            cloudStorageService.deleteFile(book.getCoverImageUrl());
//        }
//
//        bookRepository.delete(book);
//    }
//}
