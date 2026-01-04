package com.example.bookverseserver.dto.response.Review;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HelpfulVoteResponse {
  Long reviewId;
  Integer helpfulCount;
  Boolean userHasVoted;
}
