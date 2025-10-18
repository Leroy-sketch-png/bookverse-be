package com.example.bookverseserver.dto.request.Authentication;

import java.util.List;

import com.example.bookverseserver.dto.request.Authentication.Recipient;
import com.example.bookverseserver.dto.request.Authentication.Sender;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailRequest {
    Sender sender;
    List<Recipient> to;
    String subject;
    String htmlContent;
}
