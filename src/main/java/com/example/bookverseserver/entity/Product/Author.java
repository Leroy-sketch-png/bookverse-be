package com.example.bookverseserver.entity.Product;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = "bookMetas")
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

    @Column(length = 1000) // URL length may exceed 255
    String avatar;

    @Column(columnDefinition = "TEXT") // biography can be very long
    String bio;
    
    @Column(length = 255)
    String position; // Role/Genre the author writes
    
    @Column(name = "books_count")
    @Builder.Default
    Integer booksCount = 0;
    
    @Column(name = "main_genre", length = 100)
    String mainGenre;
    
    @Column(columnDefinition = "TEXT")
    List<String> awards; // JSON array or comma-separated
    
    @Column(length = 255)
    String nationality;
    
    @Column(name = "date_of_birth")
    String dob; // ISO string format

    @Column(length = 500)
    String website;

    // Legacy fields - keep for backward compatibility
    String birthDate;
    String deathDate;

    @Column(length = 500)
    String topWork;

    Integer workCount;

    @ManyToMany(mappedBy = "authors")
    @JsonBackReference
    Set<BookMeta> bookMetas;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
