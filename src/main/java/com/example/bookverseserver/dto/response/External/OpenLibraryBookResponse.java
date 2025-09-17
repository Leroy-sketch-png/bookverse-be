package com.example.bookverseserver.dto.response.External;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenLibraryBookResponse {
    private String id;
    private String title;
    private String description;
    private String coverImageUrl;
    private Integer editionCount;
    private String publishedDate;
    private String openLibraryId;
}
