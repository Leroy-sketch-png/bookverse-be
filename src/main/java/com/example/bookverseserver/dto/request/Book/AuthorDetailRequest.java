package com.example.bookverseserver.dto.request.Book;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthorDetailRequest {
    String openLibraryId;   // e.g. "OL23919A"
    String name;            // e.g. "J. K. Rowling"
    String personalName;    // e.g. "Joanne Rowling"
    String birthDate;       // e.g. "31 July 1965"
    String deathDate;       // e.g. "Unknown"
    String biography;       // long text
    String avatarUrl;       // e.g. "https://covers.openlibrary.org/a/id/5543033-L.jpg"
    String nationality;     // e.g. "British"
}
