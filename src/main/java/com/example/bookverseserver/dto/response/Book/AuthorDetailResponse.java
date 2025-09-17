package com.example.bookverseserver.dto.response.Book;

import com.example.bookverseserver.dto.response.External.OpenLibraryBookResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthorDetailResponse {

    Long id;
    String openLibraryId;

    String name;
    String personalName;
    String birthDate;
    String deathDate;

    String biography;
    String avatarUrl;
    String nationality;

//    String topWork;
//    Integer workCount;

    // Optional: to show list of works from OpenLibrary
    List<OpenLibraryBookResponse> books;
}
