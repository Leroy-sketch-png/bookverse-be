package com.example.bookverseserver.dto.response.External;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OpenLibraryDetailAuthorResponse {
    private String key;
    private String name;

    @JsonProperty("personal_name")
    private String personalName;

    @JsonProperty("birth_date")
    private String birthDate;

    @JsonProperty("death_date")
    private String deathDate;

    // Bio can sometimes be a string or an object with "value"
    private Object bio;

    private List<Integer> photos;
}
