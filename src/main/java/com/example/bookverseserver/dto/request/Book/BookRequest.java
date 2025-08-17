package com.example.bookverseserver.dto.request.Book;

import lombok.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {
    private UUID sellerId;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private BigDecimal price;
    private UUID categoryId;
    private String coverImageUrl;
    private Date publishedDate;
}
