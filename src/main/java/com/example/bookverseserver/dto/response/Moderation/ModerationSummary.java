package com.example.bookverseserver.dto.response.Moderation;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ModerationSummary {
    QueueStats flaggedListings;
    QueueStats reports;
    QueueStats disputes;
    List<RecentAction> recentActions;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QueueStats {
        Long pending;
        Long reviewing;
        Long critical;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentAction {
        String type;           // action type: APPROVE, REJECT, WARN, etc.
        String targetType;     // flagged_listing, user_report, dispute
        Long targetId;
        String moderatorName;
        LocalDateTime timestamp;
        String description;    // Human-readable summary
    }
}
