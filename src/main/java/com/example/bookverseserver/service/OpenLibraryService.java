package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.External.OpenLibraryAuthorWorkResponse;
import com.example.bookverseserver.dto.response.External.OpenLibraryDetailAuthorResponse;
import com.example.bookverseserver.dto.response.External.OpenLibraryResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenLibraryService {

    private static final String BASE_URL = "https://openlibrary.org";

    public OpenLibraryDetailAuthorResponse getAuthorByOLID(String openLibraryId) {
        String url = BASE_URL + "/authors/" + openLibraryId + ".json";
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, OpenLibraryDetailAuthorResponse.class);
    }

    public OpenLibraryAuthorWorkResponse getAuthorWorks(String openLibraryId) {
        String url = BASE_URL + "/authors/" + openLibraryId + "/works.json";
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, OpenLibraryAuthorWorkResponse.class);
    }

    RestTemplate restTemplate = new RestTemplate();
    public void populateWorkDetails(OpenLibraryAuthorWorkResponse.Entry work) {
        try {
            String key = work.getKey().startsWith("/works/") ? work.getKey().substring(7) : work.getKey();
            String url = "https://openlibrary.org/works/" + key + "/editions.json";

            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            if (response != null && response.has("entries")) {
                JsonNode entries = response.get("entries");
                work.setEdition_count(entries.size());

                if (entries.size() > 0) {
                    JsonNode firstEdition = entries.get(0);

                    // Description
                    if (firstEdition.has("description")) {
                        JsonNode descNode = firstEdition.get("description");
                        if (descNode.isTextual()) {
                            work.setDescription(descNode.toString());
                        } else if (descNode.has("value")) {
                            work.setDescription(descNode.get("value").toString());
                        }
                    }

                    // Covers
                    if (firstEdition.has("covers") && firstEdition.get("covers").isArray()) {
                        List<Integer> covers = new ArrayList<>();
                        firstEdition.get("covers").forEach(n -> covers.add(n.asInt()));
                        work.setCovers(covers);

                        // Set the first cover as coverImageUrl
                        if (!covers.isEmpty()) {
                            String coverUrl = "https://covers.openlibrary.org/b/id/" + covers.get(0) + "-L.jpg";
                            work.setCoverImageUrl(coverUrl);
                        }
                    }
                }
            } else {
                work.setEdition_count(0);
            }
        } catch (Exception e) {
            work.setEdition_count(0);
        }
    }

    // Populate covers if missing
    public void populateCovers(OpenLibraryAuthorWorkResponse.Entry work) {
        if (work.getCovers() == null || work.getCovers().isEmpty()) {
            try {
                String key = work.getKey().startsWith("/works/") ? work.getKey().substring(7) : work.getKey();
                String url = BASE_URL + "/works/" + key + ".json";

                JsonNode response = restTemplate.getForObject(url, JsonNode.class);
                if (response != null && response.has("covers")) {
                    JsonNode coversNode = response.get("covers");
                    if (coversNode.isArray()) {
                        List<Integer> covers = new ArrayList<>();
                        coversNode.forEach(n -> covers.add(n.asInt()));
                        work.setCovers(covers);
                    }
                }
            } catch (Exception e) {
                // leave covers empty if fetch fails
            }
        }
    }

    public List<OpenLibraryResponse> getAuthorsByName(String name) {
        String url = BASE_URL + "/search/authors.json?q=" + name;
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, Object>> docs = (List<Map<String, Object>>) response.get("docs");

        // Map docs → OpenLibraryResponse
        return docs.stream()
                .map(doc -> {
                    OpenLibraryResponse dto = new OpenLibraryResponse();
                                dto.setKey((String) doc.get("key"));
                                dto.setName((String) doc.get("name"));
                                dto.setTopWork((String) doc.get("top_work"));
                                dto.setWorkCount((Integer) doc.get("work_count"));
                                return dto;
                            })
                            .toList();
    }
}
