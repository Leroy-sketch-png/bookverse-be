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
    String name;
    String avatar; // Match frontend naming (not avatarUrl)
    String bio; // Match frontend naming (not biography)
    String position; // Role/Genre
    Integer booksCount;
    String mainGenre;
    List<String> awards; // Can be parsed from JSON string
    String nationality;
    String dob; // Date of birth in ISO string format
    String website;
    
    // Legacy fields for backward compatibility
    String openLibraryId;
    String topWork;
    Integer workCount;

    public AuthorResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
