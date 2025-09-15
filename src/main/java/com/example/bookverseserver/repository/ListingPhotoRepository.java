package com.example.bookverseserver.repository;

import com.example.bookverseserver.entity.Product.ListingPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListingPhotoRepository extends JpaRepository<ListingPhoto, Long> {
}
