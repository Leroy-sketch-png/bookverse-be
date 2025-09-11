package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Product.BookMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookMetaRepository extends JpaRepository<BookMeta, Long> {

    // Find by category id (ManyToMany relation -> categories)
    List<BookMeta> findDistinctByCategories_Id(Long categoryId);

    // Find by category name (case-insensitive)
    List<BookMeta> findDistinctByCategories_NameIgnoreCase(String name);
}
