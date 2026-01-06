package com.example.bookverseserver.dto.response.Moderation;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ModerationSummary {
    QueueStats flaggedListings;
    QueueStats reports;
    QueueStats disputes;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QueueStats {
        Long pending;
        Long reviewing;
        Long critical;
    }
}
