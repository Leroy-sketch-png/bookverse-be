package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Voucher.VoucherRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Voucher.VoucherResponse;
import com.example.bookverseserver.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Voucher", description = "Voucher management APIs - Admin can create/delete vouchers")
public class VoucherController {

    VoucherService voucherService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get all vouchers", description = "Retrieves all vouchers. Requires ADMIN role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vouchers retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    public ApiResponse<List<VoucherResponse>> getAllVouchers() {
        return ApiResponse.<List<VoucherResponse>>builder()
                .code(1000)
                .message("Vouchers retrieved successfully!")
                .result(voucherService.getAllVouchers())
                .build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Create a new voucher", description = "Creates a new voucher with discount configuration. Requires ADMIN role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Voucher created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Voucher code already exists")
    })
    public ApiResponse<VoucherResponse> createVoucher(
            @RequestBody @Valid VoucherRequest voucherRequest) {
        return ApiResponse.<VoucherResponse>builder()
                .code(1000)
                .message("Voucher created successfully!")
                .result(voucherService.createVoucher(voucherRequest))
                .build();
    }

    @GetMapping("/{code}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get voucher by code", description = "Retrieves voucher information by its unique code")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Voucher found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Voucher not found")
    })
    public ApiResponse<VoucherResponse> getVoucher(
            @Parameter(description = "Voucher code", example = "SUMMER20") @PathVariable("code") String code) {
        return ApiResponse.<VoucherResponse>builder()
                .code(1000)
                .message("Voucher found!")
                .result(voucherService.getVoucherByCode(code))
                .build();
    }
    
    @GetMapping("/available")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get available vouchers", description = "Returns active vouchers that users can apply. No auth required.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Available vouchers retrieved")
    })
    public ApiResponse<List<VoucherResponse>> getAvailableVouchers() {
        return ApiResponse.<List<VoucherResponse>>builder()
                .code(1000)
                .message("Available vouchers retrieved!")
                .result(voucherService.getAvailableVouchers())
                .build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Delete a voucher", description = "Deletes a voucher by its ID. Requires ADMIN role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Voucher deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Voucher not found")
    })
    public void deleteVoucher(
            @Parameter(description = "Voucher ID", example = "1") @PathVariable("id") Long id) {
        voucherService.deleteVoucher(id);
    }
    
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update a voucher", description = "Updates an existing voucher. Requires ADMIN role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Voucher updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Voucher not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Voucher code already exists")
    })
    public ApiResponse<VoucherResponse> updateVoucher(
            @Parameter(description = "Voucher ID", example = "1") @PathVariable("id") Long id,
            @RequestBody @Valid VoucherRequest voucherRequest) {
        return ApiResponse.<VoucherResponse>builder()
                .code(1000)
                .message("Voucher updated successfully!")
                .result(voucherService.updateVoucher(id, voucherRequest))
                .build();
    }
}
