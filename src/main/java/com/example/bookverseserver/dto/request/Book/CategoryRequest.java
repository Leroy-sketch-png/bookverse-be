package com.example.bookverseserver.dto.request.Book;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request body for creating or updating a category")
public class CategoryRequest {

    @Schema(
            description = "Category name",
            example = "Fantasy"
    )
    String name;

    @Schema(
            description = "Category description",
            example = "Books related to fantasy worlds and magic"
    )
    String description;

    @Schema(
            description = "Tags for searching and filtering",
            example = "[\"magic\", \"adventure\", \"myth\"]"
    )
    List<String> tags;
}
