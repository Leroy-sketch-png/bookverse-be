package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.External.OpenLibraryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenLibraryService {

    private static final String BASE_URL = "https://openlibrary.org";

    public OpenLibraryResponse getAuthorByOLID(String openLibraryId) {
        String url = BASE_URL + "/authors/" + openLibraryId + ".json";
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, OpenLibraryResponse.class);
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
