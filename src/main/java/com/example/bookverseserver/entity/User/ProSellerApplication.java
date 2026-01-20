package com.example.bookverseserver.entity.User;

import com.example.bookverseserver.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pro_seller_application")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProSellerApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;
    
    @Column(name = "business_name", nullable = false, length = 255)
    String businessName;
    
    @Column(name = "business_address", nullable = false, columnDefinition = "TEXT")
    String businessAddress;
    
    @Column(name = "business_phone", nullable = false, length = 20)
    String businessPhone;
    
    @Column(name = "tax_id", nullable = false, length = 50)
    String taxId;
    
    @Column(name = "business_license_number", length = 50)
    String businessLicenseNumber;
    
    @Column(name = "business_description", columnDefinition = "TEXT")
    String businessDescription;
    
    @Column(name = "years_in_business")
    Integer yearsInBusiness;
    
    @Column(name = "monthly_inventory")
    Integer monthlyInventory;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pro_seller_documents", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "document_url", columnDefinition = "TEXT")
    @Builder.Default
    List<String> documentUrls = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    ApplicationStatus status = ApplicationStatus.PENDING;
    
    @Column(name = "review_notes", columnDefinition = "TEXT")
    String reviewNotes;
    
    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    LocalDateTime submittedAt;
    
    @Column(name = "reviewed_at")
    LocalDateTime reviewedAt;
    
    @Column(name = "reviewed_by")
    Long reviewedBy;
}
