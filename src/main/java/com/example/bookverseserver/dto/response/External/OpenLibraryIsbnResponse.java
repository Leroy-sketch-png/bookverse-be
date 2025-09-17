package com.example.bookverseserver.dto.response.External;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryIsbnResponse {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookData {
        private String bib_key;
        private String preview;
        private String preview_url;
        private String info_url;
        private BookDetails details;
        private Cover cover; // Added cover field

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Cover {
            private String small;
            private String medium;
            private String large;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookDetails {
        private String title;
        private List<AuthorDetails> authors;
        private List<PublisherDetails> publishers;
        private String publish_date;
        private List<String> isbn_10;
        private List<String> isbn_13;
        private Integer number_of_pages;
        private String description;
        private List<SubjectDetails> subjects;


        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class AuthorDetails {
            private String key;
            private String name;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class PublisherDetails {
            private String name;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class SubjectDetails {
            private String name;
        }
    }
}
