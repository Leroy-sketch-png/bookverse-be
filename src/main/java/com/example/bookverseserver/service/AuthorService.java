package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Book.AuthorDetailRequest;
import com.example.bookverseserver.dto.request.Book.AuthorRequest;
import com.example.bookverseserver.dto.response.Book.AuthorDetailResponse;
import com.example.bookverseserver.dto.response.Book.AuthorResponse;
import com.example.bookverseserver.dto.response.External.*;
import com.example.bookverseserver.entity.Product.Author;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.AuthorMapper;
import com.example.bookverseserver.mapper.OpenLibraryMapper;
import com.example.bookverseserver.repository.AuthorRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class AuthorService {
    AuthorRepository authorRepository;

    @Autowired
    AuthorMapper authorMapper;

    @Autowired
    OpenLibraryService openLibraryService;

    public AuthorDetailResponse getAuthorByOLID(String openLibraryId) {
        // 1. Fetch works directly from OpenLibrary API
        List<OpenLibraryBookResponse> bookResponses = getFilteredWorks(openLibraryId);

        // 2. Fetch author from DB if exists, otherwise fetch from OpenLibrary and save
        Optional<Author> optionalAuthor = authorRepository.findByOpenLibraryId(openLibraryId);

        Author author;
        if (optionalAuthor.isPresent()) {
            author = optionalAuthor.get();
        } else {
            OpenLibraryDetailAuthorResponse dto = openLibraryService.getAuthorByOLID(openLibraryId);
            author = OpenLibraryMapper.toEntityDetail(dto);
            author = authorRepository.save(author);
        }

        // 3. Map to AuthorDetailResponse and set works
        AuthorDetailResponse response = authorMapper.toAuthorDetailResponse(author);
        response.setBooks(bookResponses); // always from OpenLibrary
        return response;
    }

    public List<OpenLibraryBookResponse> getFilteredWorks(String openLibraryId) {
        var works = openLibraryService.getAuthorWorks(openLibraryId).getEntries();

        works.forEach(openLibraryService::populateWorkDetails);

        System.out.println("Total works from OpenLibrary: " + works.size());

        return works.stream()
                .filter(entry -> entry.getEdition_count() != null
                        && entry.getEdition_count() > 1
                        && entry.getDescription() != null
                        && entry.getCovers() != null
                        && !entry.getCovers().isEmpty())
                .map(OpenLibraryMapper::toBookResponse) // works because entry is Entry
                .peek(br -> System.out.println("Mapped BookResponse: " + br))
                .toList();

    }

    public List<AuthorResponse> getAuthorsByName(String name) {
        List<OpenLibraryResponse> dtos = openLibraryService.getAuthorsByName(name);

        List<Author> authors = dtos.stream()
                .map(OpenLibraryMapper::toEntity)
                .toList();

        if (authors.isEmpty()) {
            throw new AppException(ErrorCode.NO_AUTHOR_FOUND);
        }

        // Map each Author → AuthorResponse
        return authors.stream()
                .map(authorMapper::toAuthorResponse)
                .toList();
    }



    public List<AuthorResponse> getAllAuthors() {
        List<Author> authors = authorRepository.findAll();
        if (authors.isEmpty()) {
            throw new AppException(ErrorCode.NO_AUTHOR_FOUND);
        }
        return authors.stream()
                .map(authorMapper::toAuthorResponse)
                .toList();
    }

    public List<AuthorResponse> getAllAuthorsByNameOrNationality(String name, String nationality) {
        return authorRepository.findAuthorsByNameOrNationalityIgnoreCase(name, nationality)
                .stream()
                .map(authorMapper::toAuthorResponse)
                .toList();
    }

    public List<AuthorResponse> getAllAuthorsByNationality(String nationality) {
        return authorRepository.findAuthorsByNationalityIgnoreCase(nationality)
                .stream()
                .map(authorMapper::toAuthorResponse)
                .toList();
    }

    public List<AuthorResponse> getAllAuthorsByName(String name) {
        return authorRepository.findAuthorsByNameIgnoreCase(name)
                .stream()
                .map(authorMapper::toAuthorResponse)
                .toList();
    }

    public AuthorResponse getAuthorById(Long id) {
         Author author = authorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));
        return authorMapper.toAuthorResponse(author);
    }


    // ** ADMIN ** //
    @PreAuthorize("hasRole('ADMIN')")
    public AuthorResponse addAuthor(AuthorRequest authorRequest) {
        Author author = authorMapper.toAuthor(authorRequest);

        try {
            author = authorRepository.save(author);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.AUTHOR_EXISTED);
        }

        return authorMapper.toAuthorResponse(author);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public AuthorDetailResponse updateAuthor(String OLID, AuthorDetailRequest authorRequest) {
        Author author = authorRepository.findByOpenLibraryId(OLID)
                .orElseThrow(() -> new AppException(ErrorCode.AUTHOR_NOT_EXISTED));
        authorMapper.updateAuthor(author, authorRequest);
        return authorMapper.toAuthorDetailResponse(authorRepository.save(author));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public AuthorDetailResponse deleteAuthor(String OLID) {
        Author author = authorRepository.findByOpenLibraryId(OLID)
                .orElseThrow(() -> new AppException(ErrorCode.AUTHOR_NOT_EXISTED));
        authorRepository.delete(author);
        return authorMapper.toAuthorDetailResponse(author);
    }

    public Author getOrCreateAuthor(String name, String openLibraryKey) {
        // Try to find by OpenLibrary ID first
        if (openLibraryKey != null && !openLibraryKey.isEmpty()) {
            String olKey = openLibraryKey.replace("/authors/", "");
            Optional<Author> existingAuthor = authorRepository.findByOpenLibraryId(olKey);
            if (existingAuthor.isPresent()) {
                return existingAuthor.get();
            }
        }

        // If not found by OpenLibrary ID, try to find by name
        if (name != null && !name.isEmpty()) {
            Optional<Author> existingAuthor = authorRepository.findByName(name);
            if (existingAuthor.isPresent()) {
                // If found by name, update the OpenLibrary ID if it's missing
                Author author = existingAuthor.get();
                if (author.getOpenLibraryId() == null && openLibraryKey != null && !openLibraryKey.isEmpty()) {
                    author.setOpenLibraryId(openLibraryKey.replace("/authors/", ""));
                    return authorRepository.save(author);
                }
                return author;
            }
        }

        // If author does not exist, create a new one
        Author newAuthor = Author.builder()
                .name(name)
                .openLibraryId(openLibraryKey != null ? openLibraryKey.replace("/authors/", "") : null)
                .build();
        return authorRepository.save(newAuthor);
    }
}
