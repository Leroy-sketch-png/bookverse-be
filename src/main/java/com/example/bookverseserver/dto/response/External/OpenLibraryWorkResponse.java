
package com.example.bookverseserver.dto.response.External;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryWorkResponse {
    private JsonNode description;

    public String getFullDescription() {
        if (description == null) {
            return null;
        }
        if (description.isTextual()) {
            return description.asText();
        }
        if (description.isObject() && description.has("value")) {
            return description.get("value").asText();
        }
        return null;
    }
}
