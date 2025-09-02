package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.response.External.OpenLibraryResponse;
import com.example.bookverseserver.entity.Product.Author;

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
}
