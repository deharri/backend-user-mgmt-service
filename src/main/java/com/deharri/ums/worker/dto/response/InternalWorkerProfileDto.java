package com.deharri.ums.worker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalWorkerProfileDto {
    private UUID workerId;
    private UUID userId;
    private String username;
    private String firstName;
    private String lastName;
    private String profilePicturePath;
    private String workerType;
    private List<String> skills;
    private String bio;
    private Integer experienceYears;
    private BigDecimal hourlyRate;
    private BigDecimal dailyRate;
    private String city;
    private String area;
    private List<String> serviceCities;
    private List<String> languages;
    private String availabilityStatus;
    private Boolean isVerified;
    private BigDecimal averageRating;
    private Integer totalJobsCompleted;
    private String agencyName;
}
