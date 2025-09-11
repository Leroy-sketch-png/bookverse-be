package com.example.bookverseserver.entity.Product;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "author")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "openlibrary_id", unique = true, length = 50)
    String openLibraryId; // short IDs like OL23919A

    @Column(unique = true, length = 500) // names can be long
    String name;

    @Column(length = 500)
    String personalName;

    String birthDate;
    String deathDate;

    @Column(length = 500)
    String topWork;

    Integer workCount;

    @Column(columnDefinition = "TEXT") // biography can be very long
    String biography;

    @Column(length = 1000) // URL length may exceed 255
    String avatarUrl;

    @Column(length = 255)
    String nationality;

    @ManyToMany(mappedBy = "authors")
    Set<BookMeta> bookMetas;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
