package com.example.bookverseserver.dto.response.Admin;

import com.example.bookverseserver.enums.ApplicationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Detailed PRO seller application for admin review.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProApplicationDetailResponse {
    Long id;
    Long userId;
    String username;
    String email;
    String businessName;
    String businessAddress;
    String businessPhone;
    String taxId;
    String businessLicenseNumber;
    String businessDescription;
    Integer yearsInBusiness;
    Integer monthlyInventory;
    List<String> documentUrls;
    ApplicationStatus status;
    String reviewNotes;
    LocalDateTime submittedAt;
    LocalDateTime reviewedAt;
    Long reviewedBy;
}
