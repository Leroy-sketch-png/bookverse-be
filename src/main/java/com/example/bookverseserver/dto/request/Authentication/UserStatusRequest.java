package com.example.bookverseserver.dto.request.Authentication;

import lombok.Data;

@Data
public class UserStatusRequest {
    boolean enabled; // true = Active, false = Banned
}