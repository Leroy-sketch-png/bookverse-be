package com.example.bookverseserver.dto.request.Book;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
