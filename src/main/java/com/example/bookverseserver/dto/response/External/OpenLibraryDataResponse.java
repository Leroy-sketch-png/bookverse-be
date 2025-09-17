
package com.example.bookverseserver.dto.response.External;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryDataResponse {

    private String key; // e.g., /books/OL1017798M
    private String title;
    private List<Author> authors;
    @JsonProperty("number_of_pages")
    private int numberOfPages;
    private List<Publisher> publishers;
    @JsonProperty("publish_date")
    private String publishDate;
    private List<Subject> subjects;
    private Cover cover;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        private String key; // e.g., /authors/OL18319A
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Publisher {
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Subject {
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cover {
        private String small;
        private String medium;
        private String large;
    }
}
