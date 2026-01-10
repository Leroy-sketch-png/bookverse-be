package com.example.bookverseserver.dto.request.Moderation;

import com.example.bookverseserver.enums.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Request for creating a user report (listings, sellers, reviews).
 * Public endpoint - any authenticated user can submit.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateReportRequest {
    
    @NotBlank(message = "REPORTED_ENTITY_TYPE_REQUIRED")
    String entityType; // "listing", "seller", "review"
    
    @NotNull(message = "REPORTED_ENTITY_ID_REQUIRED")
    Long entityId;
    
    @NotNull(message = "REPORT_TYPE_REQUIRED")
    ReportType reportType;
    
    @NotBlank(message = "REPORT_DESCRIPTION_REQUIRED")
    @Size(min = 10, max = 2000, message = "REPORT_DESCRIPTION_LENGTH_INVALID")
    String description;
    
    // Optional: URLs to evidence (screenshots, etc.)
    List<String> evidenceUrls;
    
    // Optional: related order ID for transaction disputes
    Long relatedOrderId;
}
