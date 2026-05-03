package com.deharri.ums.worker.dto.request;

import com.deharri.ums.worker.entity.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateAvailabilityDto {

    @NotNull(message = "Availability status is required")
    private AvailabilityStatus.Status status;

    private LocalDateTime unavailableFrom;

    private LocalDateTime unavailableUntil;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String unavailabilityReason;
}
