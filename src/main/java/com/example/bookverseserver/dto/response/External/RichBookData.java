
package com.example.bookverseserver.dto.response.External;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RichBookData {
    private String title;
    private String isbn;
    private String description;
    private String publisher;
    private String publishedDate;
    private int numberOfPages;
    private String coverUrl;
    private List<String> authors;
    private List<String> categories;
    private List<String> authorKeys;
}
