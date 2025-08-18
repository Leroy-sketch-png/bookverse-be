package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Product.Author;
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
}
