package com.deharri.ums.worker.dto.response;

import com.deharri.ums.enums.Language;
import com.deharri.ums.enums.PakistanCity;
import com.deharri.ums.worker.entity.AvailabilityStatus;
import com.deharri.ums.worker.entity.CnicVerification;
import com.deharri.ums.worker.entity.Worker;
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
public class WorkerProfileResponseDto {

    private String workerId;

    // User information
    private String userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String profilePictureUrl; // Pre-signed S3 URL

    // Worker basic info
    private Worker.WorkerType workerType;
    private List<String> skills;
    private String bio;

    // Experience and Pricing
    private Integer experienceYears;
    private BigDecimal hourlyRate;
    private BigDecimal dailyRate;

    // Location
    private PakistanCity city;
    private String area;
    private List<PakistanCity> serviceCities;

    // Languages
    private List<Language> languages;

    // Portfolio
    private List<String> portfolioImageUrls; // Pre-signed S3 URLs

    // Agency
    private AgencyBasicInfoDto agency; // Basic agency info if worker belongs to one

    // Availability
    private AvailabilityStatus.Status availabilityStatus;
    private LocalDateTime unavailableFrom;
    private LocalDateTime unavailableUntil;
    private String unavailabilityReason;

    // Verification
    private boolean isVerified;
    private CnicVerification.Status verificationStatus;

    // Statistics
    private BigDecimal averageRating;
    private Integer totalJobsCompleted;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AgencyBasicInfoDto {
        private String agencyId;
        private String agencyName;
        private PakistanCity city;
    }
}
