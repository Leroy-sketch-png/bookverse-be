package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.response.External.OpenLibraryDetailAuthorResponse;
import com.example.bookverseserver.dto.response.External.OpenLibraryResponse;
import com.example.bookverseserver.entity.Product.Author;

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

}
