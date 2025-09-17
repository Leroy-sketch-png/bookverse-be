package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Book.BookDetailResponse;
import com.example.bookverseserver.dto.response.Book.BookResponse;
import com.example.bookverseserver.dto.response.Book.AuthorResponse;
import com.example.bookverseserver.dto.response.Book.CategoryResponse;
import com.example.bookverseserver.dto.response.External.RichBookData;
import com.example.bookverseserver.entity.Product.Author;
import com.example.bookverseserver.entity.Product.BookMeta;
import com.example.bookverseserver.entity.Product.Category;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.Product.BookImage;
import com.example.bookverseserver.enums.ListingStatus;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.BookMetaRepository;
import com.example.bookverseserver.repository.ListingRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookMetaRepository bookMetaRepository;
    private final ListingRepository listingRepository;

    @Autowired
    private OpenLibraryService openLibraryService;
    @Autowired
    private AuthorService authorService;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    public BookService(BookMetaRepository bookMetaRepository, ListingRepository listingRepository) {
        this.bookMetaRepository = bookMetaRepository;
        this.listingRepository = listingRepository;
    }

    public ApiResponse<Map<String, Object>> getBooks(String q, String authorId, String categoryId, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);

        Specification<BookMeta> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (StringUtils.hasText(q)) {
                Predicate titlePredicate = cb.like(root.get("title"), "%" + q + "%");
                Join<BookMeta, Author> authorJoin = root.join("authors");
                Predicate authorPredicate = cb.like(authorJoin.get("name"), "%" + q + "%");
                p = cb.and(p, cb.or(titlePredicate, authorPredicate));
            }
            if (StringUtils.hasText(authorId)) {
                Join<BookMeta, Author> authorJoin = root.join("authors");
                p = cb.and(p, cb.equal(authorJoin.get("id"), authorId));
            }
            if (StringUtils.hasText(categoryId)) {
                Join<BookMeta, Category> categoryJoin = root.join("categories");
                p = cb.and(p, cb.equal(categoryJoin.get("id"), categoryId));
            }
            return p;
        };

        Page<BookMeta> bookMetaPage = bookMetaRepository.findAll(spec, pageable);

        List<BookResponse> bookResponses = bookMetaPage.getContent().stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", bookMetaPage.getNumber());
        pagination.put("limit", bookMetaPage.getSize());
        pagination.put("total", bookMetaPage.getTotalElements());
        pagination.put("has_next", bookMetaPage.hasNext());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("data", bookResponses);
        responseData.put("pagination", pagination);

        return ApiResponse.<Map<String, Object>>builder().message("ok").result(responseData).build();
    }

    public ApiResponse<BookDetailResponse> getBookById(String bookId) {
        return bookMetaRepository.findById(bookId)
                .map(bookMeta -> ApiResponse.<BookDetailResponse>builder().message("ok").result(convertToBookDetailResponse(bookMeta)).build())
                .orElse(ApiResponse.<BookDetailResponse>builder().message("ok").result(null).build());
    }

    public String createBookFromOpenLibrary(String isbn) {
        Optional<BookMeta> existingBookMeta = bookMetaRepository.findByIsbn(isbn);
        if (existingBookMeta.isPresent()) {
            return existingBookMeta.get().getId().toString();
        }

        RichBookData bookData = openLibraryService.fetchRichBookDetailsByIsbn(isbn);
        if (bookData == null) {
            throw new AppException(ErrorCode.BOOK_NOT_FOUND_IN_OPEN_LIBRARY);
        }

        BookMeta newBookMeta = new BookMeta();
        newBookMeta.setTitle(bookData.getTitle());
        newBookMeta.setIsbn(isbn);
        newBookMeta.setPublisher(bookData.getPublisher());
        newBookMeta.setDescription(bookData.getDescription());
        newBookMeta.setPages(bookData.getNumberOfPages());

        if (bookData.getPublishedDate() != null) {
            try {
                // Handle various date formats from Open Library (e.g., "July 15, 2003", "2003", "2003-07-15")
                newBookMeta.setPublishedDate(LocalDate.parse(bookData.getPublishedDate(), DateTimeFormatter.ofPattern("MMMM d, yyyy")));
            } catch (java.time.format.DateTimeParseException e1) {
                try {
                    newBookMeta.setPublishedDate(LocalDate.parse(bookData.getPublishedDate() + "-01-01", DateTimeFormatter.ISO_DATE));
                } catch (java.time.format.DateTimeParseException e2) {
                    try {
                        newBookMeta.setPublishedDate(LocalDate.parse(bookData.getPublishedDate(), DateTimeFormatter.ISO_DATE));
                    } catch (java.time.format.DateTimeParseException e3) {
                        newBookMeta.setPublishedDate(null); // Fallback
                    }
                }
            }
        } else {
            newBookMeta.setPublishedDate(null);
        }

        Set<Author> authors = new HashSet<>();
        if (bookData.getAuthors() != null && bookData.getAuthorKeys() != null) {
            for (int i = 0; i < bookData.getAuthors().size(); i++) {
                String name = bookData.getAuthors().get(i);
                String key = bookData.getAuthorKeys().get(i);
                Author author = authorService.getOrCreateAuthor(name, key);
                authors.add(author);
            }
        }
        newBookMeta.setAuthors(authors);

        Set<Category> categories = new HashSet<>();
        if (bookData.getCategories() != null) {
            for (String categoryName : bookData.getCategories()) {
                Category category = categoryService.getOrCreateCategory(categoryName);
                categories.add(category);
            }
        }
        newBookMeta.setCategories(categories);

        // Handle cover image
        if (bookData.getCoverUrl() != null && !bookData.getCoverUrl().isEmpty()) {
            BookImage coverImage = BookImage.builder()
                    .url(bookData.getCoverUrl())
                    .bookMeta(newBookMeta) // Establish the relationship
                    .isCover(true) // Mark as cover image
                    .build();
            newBookMeta.getImages().add(coverImage);
        }

        BookMeta savedBookMeta = bookMetaRepository.save(newBookMeta);
        return savedBookMeta.getId().toString();
    }

    private BookResponse convertToBookResponse(BookMeta bookMeta) {
        BookResponse bookResponse = new BookResponse();
        bookResponse.setId(bookMeta.getId());
        bookResponse.setTitle(bookMeta.getTitle());
        bookResponse.setIsbn(bookMeta.getIsbn());
        bookResponse.setAuthors(bookMeta.getAuthors().stream()
                .map(author -> new AuthorResponse(author.getId(), author.getName()))
                .collect(Collectors.toList()));
        bookResponse.setCategories(bookMeta.getCategories().stream()
                .map(category -> new CategoryResponse(category.getId(), category.getName()))
                .collect(Collectors.toList()));
        bookResponse.setCover_url(bookMeta.getCoverImageUrl());

        List<Listing> listings = listingRepository.findByBookMetaAndStatusAndVisibility(bookMeta, ListingStatus.ACTIVE, true);
        if (!listings.isEmpty()) {
            Listing cheapestListing = listings.stream()
                    .min(Comparator.comparing(Listing::getPrice))
                    .orElse(null);
            if (cheapestListing != null) {
                Map<String, Object> cheapestListingPreview = new HashMap<>();
                cheapestListingPreview.put("listing_id", cheapestListing.getId().toString());
                cheapestListingPreview.put("price", cheapestListing.getPrice().toString());
                cheapestListingPreview.put("currency", cheapestListing.getCurrency());
                bookResponse.setCheapest_listing_preview(cheapestListingPreview);
            }
        }

        return bookResponse;
    }

    private BookDetailResponse convertToBookDetailResponse(BookMeta bookMeta) {
        BookDetailResponse bookDetailResponse = new BookDetailResponse();
        bookDetailResponse.setId(bookMeta.getId());
        bookDetailResponse.setTitle(bookMeta.getTitle());
        bookDetailResponse.setIsbn(bookMeta.getIsbn());
        bookDetailResponse.setPublisher(bookMeta.getPublisher());

        if (bookMeta.getPublishedDate() != null) {
            bookDetailResponse.setPublished_date(java.sql.Date.valueOf(bookMeta.getPublishedDate()));
        } else {
            bookDetailResponse.setPublished_date(null);
        }

        bookDetailResponse.setPages(bookMeta.getPages());
        bookDetailResponse.setDescription(bookMeta.getDescription());

        if (bookMeta.getAuthors() != null) {
            bookDetailResponse.setAuthors(
                    bookMeta.getAuthors().stream()
                            .map(author -> new AuthorResponse(author.getId(), author.getName()))
                            .collect(Collectors.toList())
            );
        } else {
            bookDetailResponse.setAuthors(Collections.emptyList());
        }

        if (bookMeta.getCategories() != null) {
            bookDetailResponse.setCategories(
                    bookMeta.getCategories().stream()
                            .map(category -> new CategoryResponse(category.getId(), category.getName()))
                            .collect(Collectors.toList())
            );
        } else {
            bookDetailResponse.setCategories(Collections.emptyList());
        }

        if (bookMeta.getImages() != null) {
            bookDetailResponse.setImages(
                    bookMeta.getImages().stream()
                            .map(image -> {
                                Map<String, String> imageMap = new HashMap<>();
                                imageMap.put("id", image.getId() != null ? image.getId().toString() : null);
                                imageMap.put("url", image.getUrl());
                                return imageMap;
                            })
                            .collect(Collectors.toList())
            );
        } else {
            bookDetailResponse.setImages(Collections.emptyList());
        }

        List<Listing> listings = listingRepository.findByBookMetaAndStatusAndVisibility(bookMeta, ListingStatus.ACTIVE, true);
        if (listings != null && !listings.isEmpty()) {
            Listing cheapestListing = listings.stream()
                    .min(Comparator.comparing(Listing::getPrice))
                    .orElse(null);
            if (cheapestListing != null) {
                Map<String, Object> cheapestListingPreview = new HashMap<>();
                cheapestListingPreview.put("listing_id", cheapestListing.getId() != null ? cheapestListing.getId().toString() : null);
                cheapestListingPreview.put("price", cheapestListing.getPrice() != null ? cheapestListing.getPrice().toString() : null);
                cheapestListingPreview.put("currency", cheapestListing.getCurrency());
                bookDetailResponse.setCheapest_listing_preview(cheapestListingPreview);
            }
        }

        return bookDetailResponse;
    }
}