package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.External.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpenLibraryService {

    private static final String BASE_URL = "https://openlibrary.org";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RichBookData fetchRichBookDetailsByIsbn(String isbn) {
        // Step 1: Fetch initial data using the powerful 'data' command
        OpenLibraryDataResponse dataResponse = fetchDataResponse(isbn);
        if (dataResponse == null) {
            return null; // Book not found
        }

        String description = fetchDescription(dataResponse.getKey());

        // Step 4: Consolidate all data into RichBookData
        return buildRichBookData(isbn, dataResponse, description);
    }

    private OpenLibraryDataResponse fetchDataResponse(String isbn) {
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/api/books")
                .queryParam("bibkeys", "ISBN:" + isbn)
                .queryParam("format", "json")
                .queryParam("jscmd", "data")
                .toUriString();

        JsonNode response = restTemplate.getForObject(url, JsonNode.class);
        if (response == null || !response.has("ISBN:" + isbn)) {
            return null;
        }
        return objectMapper.convertValue(response.get("ISBN:" + isbn), OpenLibraryDataResponse.class);
    }

    private String fetchDescription(String bookKey) {
        if (bookKey == null || bookKey.isEmpty()) {
            return null;
        }

        try {
            // Step 2: Get Edition details to find the Work ID
            String editionUrl = BASE_URL + bookKey + ".json";
            OpenLibraryEditionResponse editionResponse = restTemplate.getForObject(editionUrl, OpenLibraryEditionResponse.class);

            if (editionResponse == null || editionResponse.getWorks() == null || editionResponse.getWorks().isEmpty()) {
                return null;
            }
            String workKey = editionResponse.getWorks().get(0).getKey(); // e.g., /works/OL82582W

            // Step 3: Get Work details to find the description
            String workUrl = BASE_URL + workKey + ".json";
            OpenLibraryWorkResponse workResponse = restTemplate.getForObject(workUrl, OpenLibraryWorkResponse.class);

            return workResponse != null ? workResponse.getFullDescription() : null;
        } catch (Exception e) {
            // Log error, but don't block the whole process
            return null;
        }
    }

    private RichBookData buildRichBookData(String isbn, OpenLibraryDataResponse dataResponse, String description) {
        String coverUrl = null;
        if (dataResponse.getCover() != null) {
            if (dataResponse.getCover().getLarge() != null) {
                coverUrl = dataResponse.getCover().getLarge();
            } else if (dataResponse.getCover().getMedium() != null) {
                coverUrl = dataResponse.getCover().getMedium();
            } else {
                coverUrl = dataResponse.getCover().getSmall();
            }
        }

        List<String> authorNames = dataResponse.getAuthors() != null
                ? dataResponse.getAuthors().stream().map(OpenLibraryDataResponse.Author::getName).collect(Collectors.toList())
                : Collections.emptyList();

        List<String> authorKeys = dataResponse.getAuthors() != null
                ? dataResponse.getAuthors().stream().map(OpenLibraryDataResponse.Author::getKey).collect(Collectors.toList())
                : Collections.emptyList();

        List<String> categoryNames = dataResponse.getSubjects() != null
                ? dataResponse.getSubjects().stream().map(OpenLibraryDataResponse.Subject::getName).collect(Collectors.toList())
                : Collections.emptyList();

        String publisherName = (dataResponse.getPublishers() != null && !dataResponse.getPublishers().isEmpty())
                ? dataResponse.getPublishers().get(0).getName()
                : null;

        return RichBookData.builder()
                .title(dataResponse.getTitle())
                .isbn(isbn)
                .description(description)
                .publisher(publisherName)
                .publishedDate(dataResponse.getPublishDate())
                .numberOfPages(dataResponse.getNumberOfPages())
                .coverUrl(coverUrl)
                .authors(authorNames)
                .authorKeys(authorKeys)
                .categories(categoryNames)
                .build();
    }

    // --- Keeping existing author methods --- //

    public OpenLibraryDetailAuthorResponse getAuthorByOLID(String openLibraryId) {
        String url = BASE_URL + "/authors/" + openLibraryId + ".json";
        return restTemplate.getForObject(url, OpenLibraryDetailAuthorResponse.class);
    }

    public OpenLibraryAuthorWorkResponse getAuthorWorks(String openLibraryId) {
        String url = BASE_URL + "/authors/" + openLibraryId + "/works.json";
        return restTemplate.getForObject(url, OpenLibraryAuthorWorkResponse.class);
    }

    public void populateWorkDetails(OpenLibraryAuthorWorkResponse.Entry work) {
        try {
            String key = work.getKey().startsWith("/works/") ? work.getKey().substring(7) : work.getKey();
            String url = "https://openlibrary.org/works/" + key + "/editions.json";

            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            if (response != null && response.has("entries")) {
                JsonNode entries = response.get("entries");
                work.setEdition_count(entries.size());

                if (!entries.isEmpty()) {
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

    public List<OpenLibraryResponse> getAuthorsByName(String name) {
        String url = BASE_URL + "/search/authors.json?q=" + name;

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, Object>> docs = (List<Map<String, Object>>) response.get("docs");

        return docs.stream()
                .map(doc -> {
                    OpenLibraryResponse dto = new OpenLibraryResponse();
                    dto.setKey((String) doc.get("key"));
                    dto.setName((String) doc.get("name"));
                    dto.setTopWork((String) doc.get("top_work"));
                    dto.setWorkCount((Integer) doc.get("work_count"));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}