package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Book.AuthorResponse;
import com.example.bookverseserver.dto.response.Book.BookDetailResponse;
import com.example.bookverseserver.dto.response.Book.BookResponse;
import com.example.bookverseserver.dto.response.Book.CategoryResponse;
import com.example.bookverseserver.dto.response.External.RichBookData;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.entity.Product.*;
import com.example.bookverseserver.enums.ListingStatus;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.BookMetaRepository;
import com.example.bookverseserver.repository.ListingRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookMetaRepository bookMetaRepository;
    private final ListingRepository listingRepository;
    private final OpenLibraryService openLibraryService;
    private final AuthorService authorService;
    private final CategoryService categoryService;

    // --- MAIN METHOD TO FETCH & FILTER DATA ---
    public String createBookFromOpenLibrary(String isbn) {
        // 1. Check if book already exists
        Optional<BookMeta> existingBookMeta = bookMetaRepository.findByIsbn(isbn);
        if (existingBookMeta.isPresent()) {
            return existingBookMeta.get().getId().toString();
        }

        // 2. Fetch raw data from OpenLibrary
        RichBookData bookData = openLibraryService.fetchRichBookDetailsByIsbn(isbn);
        if (bookData == null) {
            throw new AppException(ErrorCode.BOOK_NOT_FOUND_IN_OPEN_LIBRARY);
        }

        // 3. Create new Book entity
        BookMeta newBookMeta = new BookMeta();
        newBookMeta.setTitle(bookData.getTitle());
        newBookMeta.setIsbn(isbn);
        newBookMeta.setPublisher(bookData.getPublisher());
        newBookMeta.setDescription(bookData.getDescription());
        newBookMeta.setPages(bookData.getNumberOfPages());
        newBookMeta.setPublishedDate(parsePublishedDate(bookData.getPublishedDate()));

        // 4. Handle Authors
        Set<Author> authors = new HashSet<>();
        if (bookData.getAuthors() != null && bookData.getAuthorKeys() != null) {
            for (int i = 0; i < bookData.getAuthors().size(); i++) {
                String name = bookData.getAuthors().get(i);
                String key = bookData.getAuthorKeys().get(i);
                authors.add(authorService.getOrCreateAuthor(name, key));
            }
        }
        newBookMeta.setAuthors(authors);

        // =====================================================================
        // 5. STRICT CATEGORY FILTERING (The Fix)
        // =====================================================================
        Set<Category> finalCategories = new HashSet<>();

        if (bookData.getCategories() != null) {
            for (String rawSubject : bookData.getCategories()) {
                // CALL THE SMART FILTER:
                // This checks if 'rawSubject' maps to one of your 6 approved Enum values.
                // If NO match found, it returns NULL.
                Category validCategory = categoryService.filterAndGetCategory(rawSubject);

                // EXCLUDE NON-MATCHES:
                // Only add to the set if it is NOT null.
                if (validCategory != null) {
                    finalCategories.add(validCategory);
                }
            }
        }
        // If the set is empty (no matches found), the book will have NO categories.
        newBookMeta.setCategories(finalCategories);
        // =====================================================================

        // 6. Handle Cover Image
        if (bookData.getCoverUrl() != null && !bookData.getCoverUrl().isEmpty()) {
            BookImage coverImage = BookImage.builder()
                    .url(bookData.getCoverUrl())
                    .bookMeta(newBookMeta)
                    .isCover(true)
                    .build();
            // Initialize list if null (depends on your Entity constructor)
            if (newBookMeta.getImages() == null) newBookMeta.setImages(new ArrayList<>());
            newBookMeta.getImages().add(coverImage);
        }

        BookMeta savedBookMeta = bookMetaRepository.save(newBookMeta);
        return savedBookMeta.getId().toString();
    }

    // --- Helper for messy dates ---
    private LocalDate parsePublishedDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        } catch (Exception e1) {
            try {
                return LocalDate.parse(dateStr + "-01-01", DateTimeFormatter.ISO_DATE);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    // --- READ OPERATIONS ---

    public ApiResponse<PagedResponse<BookResponse>> getBooks(String q, String authorId, String categoryId, int page, int limit) {
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

        // Use PagedResponse with correct meta structure
        PagedResponse<BookResponse> pagedResponse = PagedResponse.of(
                bookResponses,
                bookMetaPage.getNumber(),
                bookMetaPage.getSize(),
                bookMetaPage.getTotalElements(),
                bookMetaPage.getTotalPages()
        );

        return ApiResponse.<PagedResponse<BookResponse>>builder()
                .message("ok")
                .result(pagedResponse)
                .build();
    }

    public ApiResponse<BookDetailResponse> getBookById(String bookId) {
        return bookMetaRepository.findById(bookId)
                .map(bookMeta -> ApiResponse.<BookDetailResponse>builder().message("ok").result(convertToBookDetailResponse(bookMeta)).build())
                .orElse(ApiResponse.<BookDetailResponse>builder().message("ok").result(null).build());
    }

    private BookResponse convertToBookResponse(BookMeta bookMeta) {
        BookResponse bookResponse = new BookResponse();
        bookResponse.setId(String.valueOf(bookMeta.getId()));
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
        BookDetailResponse response = new BookDetailResponse();
        response.setId(bookMeta.getId());
        response.setTitle(bookMeta.getTitle());
        response.setIsbn(bookMeta.getIsbn());
        response.setPublisher(bookMeta.getPublisher());
        response.setPageCount(bookMeta.getPages());
        response.setLanguage(bookMeta.getLanguage()); // Add language field
        response.setDescription(bookMeta.getDescription());
        response.setPublicationDate(bookMeta.getPublishedDate() != null ? java.sql.Date.valueOf(bookMeta.getPublishedDate()) : null);

        response.setAuthors(bookMeta.getAuthors() != null ?
                bookMeta.getAuthors().stream().map(a -> new AuthorResponse(a.getId(), a.getName())).toList() :
                Collections.emptyList());

        response.setCategories(bookMeta.getCategories() != null ?
                bookMeta.getCategories().stream().map(c -> new CategoryResponse(c.getId(), c.getName())).toList() :
                Collections.emptyList());

        if (bookMeta.getImages() != null && !bookMeta.getImages().isEmpty()) {
            response.setCoverImageUrl(bookMeta.getImages().get(0).getUrl());
        }
        
        // TODO: Calculate and set averageRating and totalReviews from Review entity
        // This requires ReviewRepository injection and calculation
        response.setAverageRating(0.0);
        response.setTotalReviews(0);
        
        return response;
    }
}