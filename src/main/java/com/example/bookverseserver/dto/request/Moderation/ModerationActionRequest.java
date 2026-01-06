package com.example.bookverseserver.dto.request.Moderation;

import com.example.bookverseserver.enums.ModerationActionType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ModerationActionRequest {
    
    @NotNull(message = "ACTION_REQUIRED")
    ModerationActionType action;
    
    String note;
    
    String reason;
    
    List<String> requiredChanges; // For REQUEST_CHANGES action
    
    Integer suspensionDays; // For SUSPEND_USER action
    
    Boolean issueWarning;
    
    Boolean refundBuyer;
    
    Boolean notifyParties;
}
