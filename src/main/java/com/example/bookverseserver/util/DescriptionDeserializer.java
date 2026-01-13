package com.example.bookverseserver.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class DescriptionDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);

        if (node == null || node.isNull()) return null;

        // If description is a simple string
        if (node.isTextual()) {
            return node.asText();
        }

        // If description is an object with a "value" field
        if (node.has("value")) {
            return node.get("value").asText();
        }

        // Fallback: convert the entire node to string
        return node.toString();
    }
}
