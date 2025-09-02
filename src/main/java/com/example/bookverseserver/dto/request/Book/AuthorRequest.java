package com.example.bookverseserver.dto.request.Book;

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
public class AuthorRequest {
    String openLibraryId;

    String name;
    String topWork;
    Integer workCount;
//    String biography;
//    String avatarUrl;
//    String nationality;
}
