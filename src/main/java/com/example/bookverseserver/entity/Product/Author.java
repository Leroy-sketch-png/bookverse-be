package com.example.bookverseserver.entity.Product;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

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

    @Column(name = "openlibrary_id", unique = true)
    String openLibraryId; // this is the id of a record, keep for updating data if needed

    @Column(unique = true)
    String name;

    String personalName;
    String birthDate;
    String deathDate;

    String topWork;
    Integer workCount;

    String biography;
    String avatarUrl;
    String nationality;

    @ManyToMany(mappedBy = "authors")
    List<Book> books;
}
