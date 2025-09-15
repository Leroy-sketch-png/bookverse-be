package com.example.bookverseserver.dto.request.User;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
public class UpgradeToSellerRequest {
    @AssertTrue(message = "You must accept the terms to upgrade to a seller account.")
    private boolean acceptTerms;
}
