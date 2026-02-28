package com.deharri.ums.worker.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkerStatsDto {
    private BigDecimal averageRating;
    private Integer totalJobsCompleted;
}
