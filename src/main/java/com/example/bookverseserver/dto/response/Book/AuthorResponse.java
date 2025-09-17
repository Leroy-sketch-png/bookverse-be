package com.example.bookverseserver.dto.response.Book;

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
    String openLibraryId;

    String name;
    String topWork;
    Integer workCount;
//    String biography;
//    String avatarUrl;
//    String nationality;

public AuthorResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
