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
import com.example.bookverseserver.mapper.AuthorMapper;
import com.example.bookverseserver.repository.BookMetaRepository;
import com.example.bookverseserver.repository.ListingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final TagService tagService;
    private final AuthorMapper authorMapper;
    private final ObjectMapper objectMapper;

    // ═══════════════════════════════════════════════════════════════════════════
    // MAIN METHOD: Import book with FULL Open Library value extraction
    // ═══════════════════════════════════════════════════════════════════════════
    public String createBookFromOpenLibrary(String isbn) {
        // 1. Check if book already exists
        Optional<BookMeta> existingBookMeta = bookMetaRepository.findByIsbn(isbn);
        if (existingBookMeta.isPresent()) {
            return existingBookMeta.get().getId().toString();
        }

        // 2. Fetch rich data from OpenLibrary
        RichBookData bookData = openLibraryService.fetchRichBookDetailsByIsbn(isbn);
        if (bookData == null) {
            throw new AppException(ErrorCode.BOOK_NOT_FOUND_IN_OPEN_LIBRARY);
        }

        // 3. Create new Book entity with ALL the rich data
        BookMeta newBookMeta = new BookMeta();
        newBookMeta.setTitle(bookData.getTitle());
        newBookMeta.setIsbn(isbn);
        newBookMeta.setPublisher(bookData.getPublisher());
        newBookMeta.setDescription(bookData.getDescription());
        newBookMeta.setPages(bookData.getNumberOfPages());
        newBookMeta.setPublishedDate(parsePublishedDate(bookData.getPublishedDate()));

        // ═══════════════════════════════════════════════════════════════════════════
        // 4. AUTHORS (with full enrichment from AuthorService)
        // ═══════════════════════════════════════════════════════════════════════════
        Set<Author> authors = new HashSet<>();
        if (bookData.getAuthors() != null && bookData.getAuthorKeys() != null) {
            for (int i = 0; i < bookData.getAuthors().size(); i++) {
                String name = bookData.getAuthors().get(i);
                String key = bookData.getAuthorKeys().get(i);
                authors.add(authorService.getOrCreateAuthor(name, key));
            }
        }
        newBookMeta.setAuthors(authors);

        // ═══════════════════════════════════════════════════════════════════════════
        // 5. CATEGORIES (broad buckets: Fiction, Science, etc.)
        // ═══════════════════════════════════════════════════════════════════════════
        Set<Category> finalCategories = new HashSet<>();
        if (bookData.getCategories() != null) {
            for (String rawSubject : bookData.getCategories()) {
                Category validCategory = categoryService.filterAndGetCategory(rawSubject);
                if (validCategory != null) {
                    finalCategories.add(validCategory);
                }
            }
        }
        newBookMeta.setCategories(finalCategories);

        // ═══════════════════════════════════════════════════════════════════════════
        // 6. TAGS (granular genres: Romance, Historical, Mystery, etc.) - NEW!
        // ═══════════════════════════════════════════════════════════════════════════
        if (bookData.getCategories() != null) {
            Set<BookTag> tags = tagService.extractTags(bookData.getCategories());
            newBookMeta.setTags(tags);
            log.info("Book '{}' tagged with: {}", bookData.getTitle(), 
                    tags.stream().map(BookTag::getName).collect(Collectors.joining(", ")));
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // 7. RICH DISCOVERY DATA (NEW!)
        // ═══════════════════════════════════════════════════════════════════════════
        
        // First line (marketing gold!)
        newBookMeta.setFirstLine(bookData.getFirstLine());
        
        // Subject places (as JSON array)
        if (bookData.getSubjectPlaces() != null && !bookData.getSubjectPlaces().isEmpty()) {
            newBookMeta.setSubjectPlaces(toJson(bookData.getSubjectPlaces()));
        }
        
        // Subject people (as JSON array)
        if (bookData.getSubjectPeople() != null && !bookData.getSubjectPeople().isEmpty()) {
            newBookMeta.setSubjectPeople(toJson(bookData.getSubjectPeople()));
        }
        
        // Subject times (as JSON array)
        if (bookData.getSubjectTimes() != null && !bookData.getSubjectTimes().isEmpty()) {
            newBookMeta.setSubjectTimes(toJson(bookData.getSubjectTimes()));
        }
        
        // External links (as JSON array of objects)
        if (bookData.getExternalLinks() != null && !bookData.getExternalLinks().isEmpty()) {
            newBookMeta.setExternalLinks(toJson(bookData.getExternalLinks()));
        }
        
        // Cross-platform IDs
        newBookMeta.setOpenLibraryId(bookData.getOpenLibraryId());
        newBookMeta.setGoodreadsId(bookData.getGoodreadsId());

        // ═══════════════════════════════════════════════════════════════════════════
        // 8. COVER IMAGE
        // ═══════════════════════════════════════════════════════════════════════════
        if (bookData.getCoverUrl() != null && !bookData.getCoverUrl().isEmpty()) {
            BookImage coverImage = BookImage.builder()
                    .url(bookData.getCoverUrl())
                    .bookMeta(newBookMeta)
                    .isCover(true)
                    .build();
            if (newBookMeta.getImages() == null) newBookMeta.setImages(new ArrayList<>());
            newBookMeta.getImages().add(coverImage);
        }

        BookMeta savedBookMeta = bookMetaRepository.save(newBookMeta);
        
        log.info("✅ Imported book '{}' with full Open Library enrichment: {} categories, {} tags, {} places, {} characters",
                savedBookMeta.getTitle(),
                savedBookMeta.getCategories().size(),
                savedBookMeta.getTags() != null ? savedBookMeta.getTags().size() : 0,
                bookData.getSubjectPlaces() != null ? bookData.getSubjectPlaces().size() : 0,
                bookData.getSubjectPeople() != null ? bookData.getSubjectPeople().size() : 0);
        
        return savedBookMeta.getId().toString();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════════════════
    
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize to JSON: {}", e.getMessage());
            return null;
        }
    }

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
        bookResponse.setId(bookMeta.getId());
        bookResponse.setTitle(bookMeta.getTitle());
        bookResponse.setIsbn(bookMeta.getIsbn());
        bookResponse.setAuthors(bookMeta.getAuthors().stream()
                .map(authorMapper::toAuthorResponse)
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
        response.setLanguage(bookMeta.getLanguage());
        response.setDescription(bookMeta.getDescription());
        response.setPublicationDate(bookMeta.getPublishedDate() != null ? java.sql.Date.valueOf(bookMeta.getPublishedDate()) : null);

        response.setAuthors(bookMeta.getAuthors() != null ?
                bookMeta.getAuthors().stream().map(authorMapper::toAuthorResponse).toList() :
                Collections.emptyList());

        response.setCategories(bookMeta.getCategories() != null ?
                bookMeta.getCategories().stream().map(c -> new CategoryResponse(c.getId(), c.getName())).toList() :
                Collections.emptyList());

        if (bookMeta.getImages() != null && !bookMeta.getImages().isEmpty()) {
            response.setCoverImageUrl(bookMeta.getImages().get(0).getUrl());
        }
        
        // ═══════════════════════════════════════════════════════════════════════════
        // GRANULAR TAGS (NEW)
        // ═══════════════════════════════════════════════════════════════════════════
        if (bookMeta.getTags() != null && !bookMeta.getTags().isEmpty()) {
            response.setTags(bookMeta.getTags().stream()
                    .map(BookTag::getName)
                    .collect(Collectors.toList()));
        }
        
        // ═══════════════════════════════════════════════════════════════════════════
        // RICH DISCOVERY DATA (NEW)
        // ═══════════════════════════════════════════════════════════════════════════
        response.setFirstLine(bookMeta.getFirstLine());
        response.setSubjectPlaces(fromJson(bookMeta.getSubjectPlaces(), List.class));
        response.setSubjectPeople(fromJson(bookMeta.getSubjectPeople(), List.class));
        response.setSubjectTimes(fromJson(bookMeta.getSubjectTimes(), List.class));
        
        // External links
        if (bookMeta.getExternalLinks() != null) {
            try {
                List<Map<String, String>> links = objectMapper.readValue(
                        bookMeta.getExternalLinks(), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                response.setExternalLinks(links.stream()
                        .map(link -> {
                            BookDetailResponse.ExternalLinkResponse elr = new BookDetailResponse.ExternalLinkResponse();
                            elr.setTitle(link.get("title"));
                            elr.setUrl(link.get("url"));
                            return elr;
                        })
                        .collect(Collectors.toList()));
            } catch (Exception e) {
                log.warn("Failed to parse external links: {}", e.getMessage());
            }
        }
        
        // Cross-platform IDs
        response.setOpenLibraryId(bookMeta.getOpenLibraryId());
        response.setGoodreadsId(bookMeta.getGoodreadsId());
        
        // ═══════════════════════════════════════════════════════════════════════════
        // RATINGS
        // ═══════════════════════════════════════════════════════════════════════════
        response.setAverageRating(bookMeta.getAverageRating() != null ? bookMeta.getAverageRating().doubleValue() : 0.0);
        response.setTotalReviews(bookMeta.getTotalReviews() != null ? bookMeta.getTotalReviews() : 0);
        
        return response;
    }
    
    @SuppressWarnings("unchecked")
    private <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.warn("Failed to parse JSON: {}", e.getMessage());
            return null;
        }
    }
}