package com.example.bookverseserver.dto.request.Book;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookImageRequest {
    String url;
    String altText;
    Boolean isCover;
    Integer position;
}
