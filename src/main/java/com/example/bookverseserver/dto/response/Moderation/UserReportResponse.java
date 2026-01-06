package com.example.bookverseserver.dto.response.Moderation;

import com.example.bookverseserver.enums.ReportPriority;
import com.example.bookverseserver.enums.ReportStatus;
import com.example.bookverseserver.enums.ReportType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserReportResponse {
    Long id;
    ReporterInfo reporter;
    ReportedEntityInfo reportedEntity;
    ReportType reportType;
    String description;
    List<String> evidenceUrls;
    ReportPriority priority;
    ReportStatus status;
    RelatedOrderInfo relatedOrder;
    String resolutionNote;
    LocalDateTime createdAt;
    LocalDateTime resolvedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReporterInfo {
        Long id;
        String name;
        ReportHistory reportHistory;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportHistory {
        Integer submitted;
        Integer valid;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportedEntityInfo {
        String type;
        Long id;
        String name;
        EntityProfile profile;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EntityProfile {
        Double rating;
        Integer totalSales;
        Integer reportCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RelatedOrderInfo {
        Long id;
        String orderNumber;
        LocalDateTime date;
    }
}
