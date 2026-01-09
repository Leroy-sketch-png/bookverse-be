package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Product.*;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.dto.response.Product.*;
import com.example.bookverseserver.entity.Product.BookMeta;
import com.example.bookverseserver.entity.Product.BookImage;
import com.example.bookverseserver.entity.Product.Likes;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.Product.ListingPhoto;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.ListingStatus;
import com.example.bookverseserver.enums.StockOperation;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.BookMetaMapper;
import com.example.bookverseserver.mapper.ListingMapper;
import com.example.bookverseserver.mapper.ListingPhotoMapper;
import com.example.bookverseserver.entity.Product.Author;
import com.example.bookverseserver.entity.Product.Category;
import com.example.bookverseserver.repository.*;
import com.example.bookverseserver.repository.specification.ListingSpecification;
import com.example.bookverseserver.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.bookverseserver.dto.response.External.RichBookData;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ListingService {
    ListingRepository listingRepository;
    ListingPhotoRepository listingPhotoRepository;
    LikesRepository likesRepository;
    UserRepository userRepository;
    BookMetaRepository bookMetaRepository;
    AuthorRepository authorRepository;
    CategoryRepository categoryRepository;
    BookMetaMapper bookMetaMapper;
    ListingMapper listingMapper;
    ListingPhotoMapper listingPhotoMapper;
    CloudStorageService cloudStorageService;
    OpenLibraryService openLibraryService;
    SecurityUtils securityUtils;

    // ============ Filtered Listings Query ============

    /**
     * Get paginated listings with optional filters and text search.
     * 
     * @param query     full-text search query (searches title, author, description)
     * @param sellerId  filter by seller
     * @param bookId    filter by book
     * @param categoryId filter by category ID
     * @param status    filter by status
     * @param sortBy    field to sort by (createdAt, price, views)
     * @param sortOrder asc or desc
     * @param page      page number (0-indexed)
     * @param size      page size
     * @return paginated listing responses
     */
    @Transactional(readOnly = true)
    public PagedResponse<ListingResponse> getListingsFiltered(
            String query,
            Long sellerId,
            Long bookId,
            Long categoryId,
            ListingStatus status,
            String sortBy,
            String sortOrder,
            int page,
            int size) {
        // Build specification
        Specification<Listing> spec = Specification.where(ListingSpecification.isNotDeleted());

        // Text search across title, author, description
        if (query != null && !query.trim().isEmpty()) {
            spec = spec.and(ListingSpecification.containsSearchText(query));
        }

        if (sellerId != null) {
            spec = spec.and(ListingSpecification.hasSeller(sellerId));
        }
        if (bookId != null) {
            spec = spec.and(ListingSpecification.hasBook(bookId));
        }
        if (categoryId != null) {
            spec = spec.and(ListingSpecification.hasCategory(categoryId));
        }
        if (status != null) {
            spec = spec.and(ListingSpecification.hasStatus(status));
        } else {
            // By default, only show visible listings for public queries
            spec = spec.and(ListingSpecification.isVisible());
        }

        // Build sort
        Sort sort = buildSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Execute query
        Page<Listing> listingPage = listingRepository.findAll(spec, pageable);

        // Map to DTOs
        List<ListingResponse> responses = listingPage.getContent().stream()
                .map(listingMapper::toListingResponse)
                .toList();

        return PagedResponse.of(
                responses,
                page,
                size,
                listingPage.getTotalElements(),
                listingPage.getTotalPages());
    }

    /**
     * Get all listings by category slug with pagination.
     */
    @Transactional(readOnly = true)
    public PagedResponse<ListingResponse> getListingsByCategory(
            String categorySlug,
            String sortBy,
            String sortOrder,
            int page,
            int size) {
        
        Sort sort = buildSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Listing> listingPage = listingRepository.findByCategorySlug(categorySlug, pageable);
        
        List<ListingResponse> responses = listingPage.getContent().stream()
                .map(listingMapper::toListingResponse)
                .toList();

        return PagedResponse.of(
                responses,
                page,
                size,
                listingPage.getTotalElements(),
                listingPage.getTotalPages());
    }

    private Sort buildSort(String sortBy, String sortOrder) {
        String field = switch (sortBy != null ? sortBy : "createdAt") {
            case "price" -> "price";
            case "viewCount", "views" -> "views";
            case "soldCount" -> "soldCount";
            default -> "createdAt";
        };

        return "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(field).ascending()
                : Sort.by(field).descending();
    }

    // ============ Listing Detail with View Count ============

    /**
     * Get listing by ID with view count increment.
     * View count is NOT incremented if the viewer is the seller.
     */
    @Transactional
    public ListingDetailResponse getListingDetail(Long listingId, Authentication authentication) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_EXISTED));

        // Increment view count if viewer is not the seller
        Long currentUserId = null;
        try {
            currentUserId = securityUtils.getCurrentUserId(authentication);
        } catch (AppException e) {
            // Anonymous user - will increment view count
        }

        if (currentUserId == null || !currentUserId.equals(listing.getSeller().getId())) {
            incrementViewCount(listingId);
            listing.setViews(listing.getViews() + 1); // Update local object for response
        }

        // Build response with related listings
        ListingDetailResponse response = listingMapper.toDetailResponse(listing);

        // Get related listings (same book, different sellers)
        List<RelatedListingDto> relatedListings = getRelatedListings(
                listing.getBookMeta().getId(),
                listingId,
                5 // Limit to 5 related listings
        );
        response.setRelatedListings(relatedListings);

        return response;
    }

    /**
     * Atomically increment view count.
     */
    @Transactional
    public void incrementViewCount(Long listingId) {
        listingRepository.incrementViewCount(listingId);
    }

    /**
     * Get related listings for the same book from different sellers.
     */
    @Transactional(readOnly = true)
    public List<RelatedListingDto> getRelatedListings(Long bookId, Long excludeListingId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Listing> related = listingRepository.findRelatedListings(bookId, excludeListingId, pageable);
        return listingMapper.toRelatedDtoList(related);
    }

    // ============ Stock Management ============

    /**
     * Update listing stock with SET, ADD, or SUBTRACT operations.
     * 
     * @param listingId the listing to update
     * @param userId    the user performing the update (must be owner)
     * @param request   the stock update request
     * @return stock update result
     */
    @Transactional
    public StockUpdateResponse updateStock(Long listingId, Long userId, UpdateStockRequest request) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND));

        // Authorization check
        if (!listing.getSeller().getId().equals(userId)) {
            throw new AppException(ErrorCode.DO_NOT_HAVE_PERMISSION);
        }

        Integer oldQuantity = listing.getQuantity();
        Integer newQuantity = calculateNewQuantity(oldQuantity, request.getQuantity(), request.getOperation());

        // Validate new quantity
        if (newQuantity < 0) {
            throw new AppException(ErrorCode.STOCK_CANNOT_BE_NEGATIVE);
        }

        listing.setQuantity(newQuantity);
        listing = listingRepository.save(listing); // @PreUpdate will handle status change

        return StockUpdateResponse.builder()
                .listingId(listingId)
                .oldQuantity(oldQuantity)
                .newQuantity(newQuantity)
                .status(listing.getStatus())
                .build();
    }

    private Integer calculateNewQuantity(Integer current, Integer amount, StockOperation operation) {
        return switch (operation) {
            case SET -> amount;
            case ADD -> current + amount;
            case SUBTRACT -> current - amount;
        };
    }

    // ============ Existing Methods (Updated) ============

    @Transactional
    public ListingResponse createListing(ListingCreationRequest request, Authentication authentication) {
        if (request.getBookMetaId() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    "A listing must be associated with an existing book. Please provide a bookMetaId.");
        }

        // Use existing bookMeta
        BookMeta bookMeta = bookMetaRepository.findById(request.getBookMetaId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_META_NOT_FOUND));

        // Create listing
        Listing listing = listingMapper.toListing(request.getListing());
        listing.setBookMeta(bookMeta);
        listing.setViews(0);
        listing.setLikes(0);
        listing.setSoldCount(0);
        listing.setSeller(userRepository.getReferenceById(securityUtils.getCurrentUserId(authentication)));
        listing = listingRepository.save(listing);

        // Create photos
        if (request.getPhotos() != null && !request.getPhotos().isEmpty()) {
            List<ListingPhoto> photos = new ArrayList<>();
            for (ListingPhotoRequest p : request.getPhotos()) {
                ListingPhoto photo = listingPhotoMapper.toListingPhoto(p);
                photo.setListing(listing);
                photos.add(listingPhotoRepository.save(photo));
            }
            listing.setPhotos(photos);
        }

        return listingMapper.toListingResponse(listing);
    }

    public ListingUpdateResponse updateListing(Long listingId, ListingUpdateRequest request,
            Authentication authentication) {
        log.info(">>> Entered updateListing with listingId={}", listingId);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND));

        Long currentUserId = securityUtils.getCurrentUserId(authentication);

        log.info("SellerId={}, CurrentUserId={}", listing.getSeller().getId(), currentUserId);
        if (listing.getSeller().getId().equals(currentUserId)) {
            listingMapper.updateListing(listing, request);
            listing = listingRepository.save(listing);
            return listingMapper.toListingUpdateResponse(listing);
        } else {
            throw new AppException(ErrorCode.DO_NOT_HAVE_PERMISSION);
        }
    }

    public String hardDeleteListing(Long listingId, Authentication authentication) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND));

        Long currentUserId = securityUtils.getCurrentUserId(authentication);

        if (listing.getSeller().getId().equals(currentUserId)) {
            listingRepository.delete(listing);
            return "Successfully deleted listing";
        } else {
            throw new AppException(ErrorCode.DO_NOT_HAVE_PERMISSION);
        }
    }

    public ListingUpdateResponse softDeleteListing(Long listingId, Authentication authentication) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_EXISTED));

        Long currentUserId = securityUtils.getCurrentUserId(authentication);

        if (!listing.getSeller().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.DO_NOT_HAVE_PERMISSION);
        }

        listing.setDeletedAt(LocalDateTime.now());
        listing.setDeletedBy(currentUserId);
        listing.setStatus(ListingStatus.REMOVED);
        listing.setVisibility(false);

        listing = listingRepository.save(listing);
        return listingMapper.toListingUpdateResponse(listing);
    }

    @Transactional
    public ListingResponse toggleListingLike(Long listingId, Authentication authentication) {
        Long currentUserId = securityUtils.getCurrentUserId(authentication);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_EXISTED));

        if (listing.getSeller().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.DO_NOT_HAVE_PERMISSION);
        }

        if (likesRepository.existsByUserIdAndListingId(currentUserId, listingId)) {
            // already liked → unlike
            likesRepository.deleteByUserIdAndListingId(currentUserId, listingId);
            listing.setLikes(listing.getLikes() - 1);
        } else {
            // not yet liked → like
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            Likes like = new Likes();
            like.setListing(listing);
            like.setUser(currentUser);

            likesRepository.save(like);
            listing.setLikes(listing.getLikes() + 1);
        }

        listingRepository.save(listing);
        return listingMapper.toListingResponse(listing);
    }

    /**
     * Get listing by ID (legacy method - now delegates to getListingDetail).
     */
    public ListingResponse getListingById(Long listingId, Authentication authentication) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_EXISTED));

        // Increment view count if viewer is not the seller
        Long currentUserId = null;
        try {
            currentUserId = securityUtils.getCurrentUserId(authentication);
        } catch (AppException e) {
            // Anonymous user
        }

        if (currentUserId == null || !currentUserId.equals(listing.getSeller().getId())) {
            listing.setViews(listing.getViews() + 1);
            listingRepository.save(listing);
        }

        return listingMapper.toListingResponse(listing);
    }

    public List<ListingResponse> getAllListings() {
        List<Listing> listings = listingRepository.findAll();
        if (listings.isEmpty()) {
            throw new AppException(ErrorCode.NO_LISTING_FOUND);
        }

        return listings.stream()
                .map(listingMapper::toListingResponse)
                .toList();
    }

    public ListingResponse updateListingSoldCount(Long listingId, Integer purchaseQuantity,
            Authentication authentication) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_EXISTED));

        if (listing.getSeller().getId().equals(securityUtils.getCurrentUserId(authentication))) {
            throw new AppException(ErrorCode.DO_NOT_HAVE_PERMISSION);
        } else {
            if (purchaseQuantity > listing.getQuantity()) {
                throw new AppException(ErrorCode.NOT_ENOUGH_LISTING);
            } else {
                listing.setSoldCount(listing.getSoldCount() + purchaseQuantity);
                listing.setQuantity(listing.getQuantity() - purchaseQuantity);
                listingRepository.save(listing);
            }
        }
        return listingMapper.toListingResponse(listing);
    }

    // ============ Simple Listing Creation (Multipart) ============

    /**
     * Create a listing with book metadata and photos in a single request.
     * Accepts flat form data + image files directly from frontend form.
     * 
     * CANONICAL FLOW (per Vision):
     * 1. If ISBN provided → Fetch from Open Library for canonical metadata
     * 2. If Open Library fails or no ISBN → Use seller-provided data (marked as unverified)
     * 3. Create Listing with seller-specific data (price, condition, photos)
     * 
     * @param request  flat listing data
     * @param images   optional image files to upload
     * @param authentication current user
     * @return created listing
     */
    @Transactional
    public ListingResponse createSimpleListing(
            SimpleListingCreationRequest request,
            List<MultipartFile> images,
            Authentication authentication) {
        
        Long userId = securityUtils.getCurrentUserId(authentication);
        User seller = userRepository.getReferenceById(userId);

        // ═══════════════════════════════════════════════════════════════════════════
        // STEP 1: Resolve BookMeta (Canonical Source: Open Library)
        // ═══════════════════════════════════════════════════════════════════════════
        BookMeta bookMeta = resolveBookMeta(request);

        // ═══════════════════════════════════════════════════════════════════════════
        // STEP 2: Create Listing (Seller-specific data)
        // ═══════════════════════════════════════════════════════════════════════════
        ListingStatus listingStatus;
        try {
            listingStatus = ListingStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            listingStatus = ListingStatus.DRAFT;
        }

        Listing listing = Listing.builder()
                .bookMeta(bookMeta)
                .seller(seller)
                .price(request.getPrice() != null ? java.math.BigDecimal.valueOf(request.getPrice()) : null)
                .originalPrice(request.getOriginalPrice() != null ? java.math.BigDecimal.valueOf(request.getOriginalPrice()) : null)
                .condition(request.getCondition())
                .currency("VND")
                .quantity(request.getStock())
                .status(listingStatus)
                .description(request.getDescription()) // Seller's description of THIS copy
                .views(0)
                .likes(0)
                .soldCount(0)
                .build();
        
        listing = listingRepository.save(listing);

        // ═══════════════════════════════════════════════════════════════════════════
        // STEP 3: Upload images and create ListingPhotos (Cloudinary)
        // ═══════════════════════════════════════════════════════════════════════════
        if (images != null && !images.isEmpty()) {
            List<ListingPhoto> photos = new ArrayList<>();
            int position = 0;
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    String imageUrl = cloudStorageService.uploadFile(image);
                    ListingPhoto photo = ListingPhoto.builder()
                            .listing(listing)
                            .url(imageUrl)
                            .position(position++)
                            .build();
                    photos.add(listingPhotoRepository.save(photo));
                }
            }
            listing.setPhotos(photos);
        }

        log.info("Created listing {} for seller {} with {} photos (bookMeta={})", 
                listing.getId(), userId, 
                listing.getPhotos() != null ? listing.getPhotos().size() : 0,
                bookMeta.getId());

        return listingMapper.toListingResponse(listing);
    }

    /**
     * Resolve BookMeta using canonical sources.
     * 
     * Priority:
     * 1. Check if BookMeta exists in DB by ISBN
     * 2. If not, try Open Library API (canonical source)
     * 3. If Open Library fails, create from seller input (unverified)
     */
    private BookMeta resolveBookMeta(SimpleListingCreationRequest request) {
        String isbn = request.getIsbn();
        
        // Case 1: ISBN provided - try canonical sources
        if (isbn != null && !isbn.isBlank()) {
            String normalizedIsbn = isbn.replaceAll("[^0-9X]", ""); // Remove hyphens/spaces
            
            // 1a. Check if we already have this book
            var existingBook = bookMetaRepository.findByIsbn(normalizedIsbn);
            if (existingBook.isPresent()) {
                log.info("Found existing BookMeta for ISBN {}", normalizedIsbn);
                return existingBook.get();
            }
            
            // 1b. Fetch from Open Library (canonical source)
            try {
                RichBookData openLibraryData = openLibraryService.fetchRichBookDetailsByIsbn(normalizedIsbn);
                if (openLibraryData != null) {
                    log.info("Fetched canonical data from Open Library for ISBN {}", normalizedIsbn);
                    return createBookMetaFromOpenLibrary(openLibraryData, normalizedIsbn);
                }
            } catch (Exception e) {
                log.warn("Open Library fetch failed for ISBN {}: {}", normalizedIsbn, e.getMessage());
                // Fall through to manual creation
            }
        }
        
        // Case 2: No ISBN or Open Library failed - create from seller input
        log.info("Creating BookMeta from seller input (no canonical source): {}", request.getTitle());
        return createBookMetaFromSellerInput(request);
    }

    /**
     * Create BookMeta from Open Library canonical data.
     */
    private BookMeta createBookMetaFromOpenLibrary(RichBookData data, String isbn) {
        // Resolve authors (create if not exist)
        var authors = new HashSet<Author>();
        if (data.getAuthors() != null) {
            for (String authorName : data.getAuthors()) {
                Author author = authorRepository.findByName(authorName)
                        .orElseGet(() -> authorRepository.save(Author.builder()
                                .name(authorName)
                                .openLibraryId(data.getAuthorKeys() != null && !data.getAuthorKeys().isEmpty() 
                                        ? data.getAuthorKeys().get(0).replace("/authors/", "") : null)
                                .build()));
                authors.add(author);
            }
        }
        
        // Resolve categories from Open Library subjects
        var categories = new HashSet<Category>();
        if (data.getCategories() != null && !data.getCategories().isEmpty()) {
            // Take first 3 subjects as categories
            data.getCategories().stream().limit(3).forEach(subject -> {
                Category cat = categoryRepository.findByName(subject)
                        .orElseGet(() -> categoryRepository.save(Category.builder()
                                .name(subject)
                                .slug(subject.toLowerCase().replaceAll("\\s+", "-"))
                                .build()));
                categories.add(cat);
            });
        }
        
        // Parse published date
        LocalDate publishedDate = null;
        if (data.getPublishedDate() != null) {
            try {
                // Open Library uses various formats, try to extract year
                String dateStr = data.getPublishedDate();
                if (dateStr.length() >= 4) {
                    int year = Integer.parseInt(dateStr.substring(0, 4));
                    publishedDate = LocalDate.of(year, 1, 1);
                }
            } catch (Exception e) {
                log.debug("Could not parse published date: {}", data.getPublishedDate());
            }
        }
        
        BookMeta bookMeta = BookMeta.builder()
                .title(data.getTitle())
                .isbn(isbn)
                .description(data.getDescription())
                .publisher(data.getPublisher())
                .publishedDate(publishedDate)
                .pages(data.getNumberOfPages() > 0 ? data.getNumberOfPages() : null)
                .authors(authors)
                .categories(categories)
                .openLibraryId(data.getOpenLibraryId())
                .goodreadsId(data.getGoodreadsId())
                .firstLine(data.getFirstLine())
                .subjectPlaces(data.getSubjectPlaces() != null ? String.join(", ", data.getSubjectPlaces()) : null)
                .subjectPeople(data.getSubjectPeople() != null ? String.join(", ", data.getSubjectPeople()) : null)
                .subjectTimes(data.getSubjectTimes() != null ? String.join(", ", data.getSubjectTimes()) : null)
                .build();
        
        // Save first to get ID
        BookMeta savedBookMeta = bookMetaRepository.save(bookMeta);
        
        // Add cover image from Open Library if available
        if (data.getCoverUrl() != null && !data.getCoverUrl().isBlank()) {
            BookImage coverImage = BookImage.builder()
                    .bookMeta(savedBookMeta)
                    .url(data.getCoverUrl())
                    .isCover(true)
                    .position(0)
                    .altText(data.getTitle() + " cover")
                    .build();
            savedBookMeta.getImages().add(coverImage);
            // Re-save with image (cascade will persist BookImage)
            savedBookMeta = bookMetaRepository.save(savedBookMeta);
        }
        
        return savedBookMeta;
    }

    /**
     * Create BookMeta from seller-provided input (fallback, unverified).
     */
    private BookMeta createBookMetaFromSellerInput(SimpleListingCreationRequest request) {
        // Resolve author
        Author author = authorRepository.findByName(request.getAuthor())
                .orElseGet(() -> authorRepository.save(Author.builder()
                        .name(request.getAuthor())
                        .build()));
        
        // Resolve category
        Category category = categoryRepository.findByName(request.getCategory())
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(request.getCategory())
                        .slug(request.getCategory().toLowerCase().replaceAll("\\s+", "-"))
                        .build()));
        
        BookMeta bookMeta = BookMeta.builder()
                .title(request.getTitle())
                .isbn(request.getIsbn())
                .description(request.getDescription())
                .publisher(request.getPublisher())
                .publishedDate(request.getPublishYear() != null ? LocalDate.of(request.getPublishYear(), 1, 1) : null)
                .authors(new HashSet<>(List.of(author)))
                .categories(new HashSet<>(List.of(category)))
                .build();
        
        return bookMetaRepository.save(bookMeta);
    }
}
