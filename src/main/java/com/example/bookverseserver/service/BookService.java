package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Book.BookRequest;
import com.example.bookverseserver.dto.response.Book.BookResponse;
import com.example.bookverseserver.entity.Product.Book;
import com.example.bookverseserver.entity.Product.Category;
import com.example.bookverseserver.entity.Product.Inventory;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.BookMapper;
import com.example.bookverseserver.repository.BookRepository;
import com.example.bookverseserver.repository.CategoryRepository;
import com.example.bookverseserver.repository.UserRepository;
import com.example.bookverseserver.service.CloudStorageService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class BookService {
    BookRepository bookRepository;
    UserRepository userRepository;
    CategoryRepository categoryRepository;

    @Autowired
    BookMapper bookMapper;

    CloudStorageService cloudStorageService;

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(bookMapper::toBookResponse)
                .toList();
    }

    public List<BookResponse> getAllBooksByCategory(Long categoryId) {
        return bookRepository.findByCategory_Id(categoryId)
                .stream()
                .map(bookMapper::toBookResponse)
                .toList();
    }

    public List<BookResponse> getAllBooksByCategoryName(String categoryName) {
        return bookRepository.findByCategory_NameIgnoreCase(categoryName)
                .stream()
                .map(bookMapper::toBookResponse)
                .toList();
    }

    public BookResponse getBookById(Long id) {
        Book book = (Book) bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));
        return bookMapper.toBookResponse(book);
    }

    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public BookResponse createBook(BookRequest request, Optional<MultipartFile> coverImage) {
        String coverUrl = coverImage.map(cloudStorageService::uploadFile).orElse(null);

        Book book = bookMapper.toBook(request);
        book.setCoverImageUrl(coverUrl);

        // Tạo Inventory cho Book
        Inventory inventory = Inventory.builder()
                .book(book)
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .build();

        book.setInventory(inventory);

        Book saved = bookRepository.save(book);
        return bookMapper.toBookResponse(saved);
    }


    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    public BookResponse createBook(BookRequest request) {
        Book book = bookMapper.toBook(request); // Map các field cơ bản

        // 1. Set seller hiện tại từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User seller = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        book.setSeller(seller);

        // 2. Set Category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        book.setCategory(category);

        // 3. Set Inventory
        Inventory inventory = Inventory.builder()
                .book(book)
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .build();
        book.setInventory(inventory);

        // 4. Upload cover image nếu có
        if (request.getCoverImageUrl() != null) {
            book.setCoverImageUrl(request.getCoverImageUrl());
        }

        book.setPrice(request.getPrice());
        book.setTitle(request.getTitle());

        // 5. Lưu book
        Book saved = bookRepository.save(book);

        // 6. Map sang response
        return bookMapper.toBookResponse(saved);
    }

    @PreAuthorize("hasAuthority('SELLER')")
    public BookResponse updateBook(Long id, BookRequest request, Optional<MultipartFile> coverImage) {
        Book book = (Book) bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));

        bookMapper.updateBook(book, request);

        coverImage.ifPresent(file -> {
            String newCover = cloudStorageService.uploadFile(file);
            cloudStorageService.deleteFile(book.getCoverImageUrl());
            book.setCoverImageUrl(newCover);
        });

        return bookMapper.toBookResponse(bookRepository.save(book));
    }

    @PreAuthorize("hasAuthority('SELLER')")
    public void deleteBook(Long id) {
        Book book = (Book) bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));

        if (book.getCoverImageUrl() != null) {
            cloudStorageService.deleteFile(book.getCoverImageUrl());
        }
        bookRepository.delete(book);
    }
}
