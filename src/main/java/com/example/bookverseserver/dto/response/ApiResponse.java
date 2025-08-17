package com.example.bookverseserver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL) // serialize sang JSON thì những cái null sẽ không kèm theo
public class ApiResponse <T> {
    int code = 1000;
    String message;
    T result;
}