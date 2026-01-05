package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Product.BookMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookMetaRepository extends JpaRepository<BookMeta, String>, JpaSpecificationExecutor<BookMeta> {

    // Custom exists check with Long id (more efficient than findById)
    boolean existsById(Long id);

    @EntityGraph(attributePaths = { "authors", "categories", "images" })
    Optional<BookMeta> findById(Long id);

    @Override
    @EntityGraph(attributePaths = { "authors", "categories", "images" })
    Page<BookMeta> findAll(Specification<BookMeta> spec, Pageable pageable);

    // Find by category id (ManyToMany relation -> categories)
    List<BookMeta> findDistinctByCategories_Id(Long categoryId);

    // Find by category name (case-insensitive)
    List<BookMeta> findDistinctByCategories_NameIgnoreCase(String name);

    Optional<BookMeta> findByIsbn(String isbn);
}