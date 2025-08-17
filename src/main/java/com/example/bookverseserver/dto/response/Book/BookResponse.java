package com.example.bookverseserver.dto.response.Book;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {
    private Long id;
    private UUID sellerId;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private BigDecimal price;
    private UUID categoryId;
    private String coverImageUrl;
    private Date publishedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
