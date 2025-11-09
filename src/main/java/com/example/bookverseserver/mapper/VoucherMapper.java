package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Voucher.VoucherRequest;
import com.example.bookverseserver.dto.response.Voucher.VoucherCartResponse;
import com.example.bookverseserver.dto.response.Voucher.VoucherResponse;
import com.example.bookverseserver.entity.Order_Payment.Voucher;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VoucherMapper {

    Voucher toVoucher(VoucherRequest request);

    VoucherResponse toVoucherResponse(Voucher voucher);

    VoucherCartResponse toVoucherCartResponse(Voucher voucher);
}

