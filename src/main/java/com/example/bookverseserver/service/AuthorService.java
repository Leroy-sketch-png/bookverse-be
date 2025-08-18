package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Book.AuthorRequest;
import com.example.bookverseserver.dto.response.Book.AuthorResponse;
import com.example.bookverseserver.entity.Product.Author;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.AuthorMapper;
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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class AuthorService {
    AuthorRepository authorRepository;

    @Autowired
    AuthorMapper authorMapper;

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

}
