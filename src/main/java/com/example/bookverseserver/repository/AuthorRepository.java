package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Product.Author;
import com.google.common.io.Files;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    List<Author> findAuthorsByNameOrNationalityIgnoreCase(String name, String nationality);

    List<Author> findAuthorsByNameIgnoreCase(String name);

    List<Author> findAuthorsByNationalityIgnoreCase(String nationality);

    boolean existsByOpenLibraryId(String openLibraryId);

    Author findAuthorByOpenLibraryId(String openLibraryId);

    Optional<Author> findByOpenLibraryId(String openLibraryId);

    Optional<Author> findByName(String name);
    
    /**
     * Case-insensitive author lookup - prevents duplicate authors like 
     * "J.K. Rowling" vs "j.k. rowling" vs "J. K. Rowling"
     */
    Optional<Author> findByNameIgnoreCase(String name);
    
    /**
     * Check if an author with this name already exists (case-insensitive).
     */
    boolean existsByNameIgnoreCase(String name);
}
