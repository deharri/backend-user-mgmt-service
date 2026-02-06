package com.deharri.ums.worker.dto.response;

import com.deharri.ums.enums.PakistanCity;
import com.deharri.ums.worker.entity.AvailabilityStatus;
import com.deharri.ums.worker.entity.Worker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkerListItemDto {

    private String workerId;

    // Basic user info
    private String firstName;
    private String lastName;
    private String profilePictureUrl;

    // Worker info
    private Worker.WorkerType workerType;
    private Integer experienceYears;
    private BigDecimal hourlyRate;
    private BigDecimal dailyRate;

    // Location
    private PakistanCity city;
    private String area;

    // Status
    private AvailabilityStatus.Status availabilityStatus;
    private boolean isVerified;

    // Statistics
    private BigDecimal averageRating;
    private Integer totalJobsCompleted;

    // Agency
    private String agencyName; // If belongs to agency

}
