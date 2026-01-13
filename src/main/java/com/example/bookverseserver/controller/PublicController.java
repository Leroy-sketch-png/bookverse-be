package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.PublicStatsResponse;
import com.example.bookverseserver.service.PublicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Public endpoints - no authentication required.
 * Exposes non-sensitive platform data for marketing and trust-building.
 */
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Public", description = "Public platform information endpoints")
@Slf4j
public class PublicController {
    
    PublicService publicService;
    
    @GetMapping("/stats")
    @Operation(
        summary = "Get public platform statistics",
        description = "Returns non-sensitive platform stats for homepage display. No auth required."
    )
    public ApiResponse<PublicStatsResponse> getPublicStats() {
        return ApiResponse.<PublicStatsResponse>builder()
                .message("Public stats retrieved successfully")
                .result(publicService.getPublicStats())
                .build();
    }
}
