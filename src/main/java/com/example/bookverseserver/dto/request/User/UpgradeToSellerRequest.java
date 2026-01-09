package com.example.bookverseserver.dto.request.User;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpgradeToSellerRequest {
    @AssertTrue(message = "You must accept the terms to upgrade to a seller account.")
    private boolean acceptTerms;
    
    @NotBlank(message = "Shop name is required")
    @Size(min = 3, max = 50, message = "Shop name must be between 3 and 50 characters")
    private String shopName;
    
    @NotBlank(message = "Shop description is required")
    @Size(min = 20, max = 500, message = "Shop description must be between 20 and 500 characters")
    private String shopDescription;
    
    @NotBlank(message = "Return policy is required")
    @Size(min = 20, max = 500, message = "Return policy must be between 20 and 500 characters")
    private String returnPolicy;
    
    @NotBlank(message = "Shipping policy is required")
    @Size(min = 20, max = 500, message = "Shipping policy must be between 20 and 500 characters")
    private String shippingPolicy;
    
    private String responseTime; // e.g., "within_1_hour", "within_6_hours", "within_24_hours"
}
