package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.response.External.OpenLibraryBookResponse;
import com.example.bookverseserver.dto.response.External.OpenLibraryAuthorWorkResponse;
import com.example.bookverseserver.dto.response.External.OpenLibraryDetailAuthorResponse;
import com.example.bookverseserver.dto.response.External.OpenLibraryResponse;
import com.example.bookverseserver.entity.Product.Author;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class OpenLibraryMapper {

    public static Author toEntity(OpenLibraryResponse dto) {
        if (dto == null) return null;

        return Author.builder()
                .openLibraryId(dto.getKey().replace("/authors/", "")) // keep only id
                .name(dto.getName())
                .topWork(dto.getTopWork())
                .workCount(dto.getWorkCount())
                .build();
    }

    public static Author toEntityDetail(OpenLibraryDetailAuthorResponse dto) {
        if (dto == null) return null;

        String bio = null;
        if (dto.getBio() != null) {
            if (dto.getBio() instanceof Map) {
                bio = (String) ((Map<?, ?>) dto.getBio()).get("value");
            } else {
                bio = dto.getBio().toString();
            }
        }

        String avatarUrl = null;
        if (dto.getPhotos() != null && !dto.getPhotos().isEmpty()) {
            avatarUrl = "https://covers.openlibrary.org/a/id/" + dto.getPhotos().get(0) + "-L.jpg";
        }

        return Author.builder()
                .openLibraryId(dto.getKey().replace("/authors/", ""))
                .name(dto.getName())
                .personalName(dto.getPersonalName())
                .birthDate(dto.getBirthDate() != null ? dto.getBirthDate() : "Unknown")
                .deathDate(dto.getDeathDate() != null ? dto.getDeathDate() : "Unknown")
                .biography(bio)
                .avatarUrl(avatarUrl)
                .nationality("Zootopia")
                .build();
    }

    public static List<OpenLibraryBookResponse> toBookResponsesFromWorks(OpenLibraryAuthorWorkResponse worksDto) {
        if (worksDto == null || worksDto.getEntries() == null) return List.of();

        return worksDto.getEntries().stream().map(entry -> OpenLibraryBookResponse.builder()
                .id(null)
                .title(entry.getTitle())
                .description(entry.getDescription() != null ? entry.getDescription().toString() : null)
                .coverImageUrl(entry.getCovers() != null && !entry.getCovers().isEmpty()
                        ? "https://covers.openlibrary.org/b/id/" + entry.getCovers().get(0) + "-L.jpg"
                        : null)
                .editionCount(entry.getEdition_count() != null ? entry.getEdition_count() : 0)
                .publishedDate(
                        entry.getFirst_publish_date() != null
                                ? entry.getFirst_publish_date()
                                : "Unknown"
                )
                .build()
        ).toList();
    }

    public static OpenLibraryBookResponse toBookResponse(OpenLibraryAuthorWorkResponse.Entry entry) {
        return OpenLibraryBookResponse.builder()
                .openLibraryId(entry.getKey())
                .title(entry.getTitle())
                .editionCount(entry.getEdition_count() != null ? entry.getEdition_count() : 0)
                .description(entry.getDescription() != null ? entry.getDescription() : null)
                .coverImageUrl(entry.getCoverImageUrl())
                .publishedDate(entry.getFirst_publish_date())
                .build();
    }
}