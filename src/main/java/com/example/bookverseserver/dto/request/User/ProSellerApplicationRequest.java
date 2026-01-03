package com.example.bookverseserver.dto.request.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProSellerApplicationRequest {
    
    @NotBlank(message = "Business name is required")
    @Size(max = 255, message = "Business name cannot exceed 255 characters")
    String businessName;
    
    @NotBlank(message = "Business address is required")
    String businessAddress;
    
    @NotBlank(message = "Business phone is required")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    String businessPhone;
    
    @NotBlank(message = "Tax ID is required")
    @Size(max = 50, message = "Tax ID cannot exceed 50 characters")
    String taxId;
    
    @Size(max = 50, message = "License number cannot exceed 50 characters")
    String businessLicenseNumber;
    
    String businessDescription;
    
    Integer yearsInBusiness;
    Integer monthlyInventory;
    
    // Document URLs will be handled separately via file upload endpoints
    List<String> documentUrls;
}
