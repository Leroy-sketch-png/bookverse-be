package com.example.bookverseserver.dto.response.Book;

import com.example.bookverseserver.entity.Product.Book;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthorResponse {
    Long id;

    String name;
    String biography;
    String avatarUrl;
    String nationality;

    List<BookResponse> books;
}
