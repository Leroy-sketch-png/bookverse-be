package com.example.bookverseserver.dto.request.Review;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HideReviewRequest {

  @NotNull(message = "Hidden flag is required")
  Boolean hidden;

  String reason;
}
