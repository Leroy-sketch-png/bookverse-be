package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Voucher.VoucherRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Voucher.VoucherResponse;
import com.example.bookverseserver.service.VoucherService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VoucherController {

    VoucherService voucherService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<VoucherResponse> createVoucher(
            @RequestBody @Valid VoucherRequest voucherRequest
    ) {
        return ApiResponse.<VoucherResponse>builder()
                .code(201)
                .message("Voucher created successfully!")
                .result(voucherService.createVoucher(voucherRequest))
                .build();
    }

    @GetMapping("/{code}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<VoucherResponse> getVoucher(
            @PathVariable("code") String code
    ) {
        return ApiResponse.<VoucherResponse>builder()
                .code(200)
                .message("Voucher found!")
                .result(voucherService.getVoucherByCode(code))
                .build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteVoucher(@PathVariable("id") Long id) {
        voucherService.deleteVoucher(id);
    }


}
