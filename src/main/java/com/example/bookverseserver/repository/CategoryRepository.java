package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Product.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    Optional<Category> findBySlug(String slug);
    
    // Hierarchical queries
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.sortOrder, c.name")
    List<Category> findRootCategories();
    
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL ORDER BY c.sortOrder, c.name")
    List<Category> findRootCategoriesWithChildren();
    
    List<Category> findByParentIdOrderBySortOrderAscNameAsc(Long parentId);
    
    @Query("SELECT c FROM Category c WHERE c.featured = true ORDER BY c.sortOrder, c.name")
    List<Category> findFeaturedCategories();
    
    @Query("SELECT c FROM Category c WHERE c.parent IS NOT NULL ORDER BY c.parent.sortOrder, c.sortOrder, c.name")
    List<Category> findAllSubcategories();
    
    // Count books per category (for display)
    @Query("SELECT c.id, COUNT(bm) FROM Category c LEFT JOIN c.bookMetas bm GROUP BY c.id")
    List<Object[]> countBooksPerCategory();
}

