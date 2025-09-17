
package com.example.bookverseserver.dto.response.External;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryEditionResponse {
    private List<Work> works;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Work {
        private String key; // e.g., /works/OL82582W
    }
}
