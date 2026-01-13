package com.example.bookverseserver.dto.response.External;

import com.example.bookverseserver.util.DescriptionDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OpenLibraryAuthorWorkResponse {
    //Map<String, String> links;
    int size;
    List<Entry> entries;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {
            String title;
            String key;

            @JsonDeserialize(using = DescriptionDeserializer.class)
            String description;

            List<Integer> covers;
            Integer edition_count;
            String first_publish_date;
            List<String> subjects;
            String coverImageUrl;
    }
}
