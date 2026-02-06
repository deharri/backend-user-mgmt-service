package com.deharri.ums.agency.dto.response;

import com.deharri.ums.agency.entity.Agency;
import com.deharri.ums.enums.PakistanCity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgencyProfileResponseDto {

    private String agencyId;

    // User information
    private String userId;
    private String username;
    private String email;
    private String phoneNumber;

    // Agency basic info
    private String agencyName;
    private String description;
    private String contactNumber;
    private String contactEmail;

    // Location
    private PakistanCity city;
    private String address;
    private List<PakistanCity> serviceCities;

    // Verification
    private String licenseNumber;
    private boolean hasLicenseDocument;
    private Agency.VerificationStatus verificationStatus;

    // Statistics
    private Integer totalWorkers;
    private BigDecimal averageRating;
    private Integer totalJobsCompleted;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
