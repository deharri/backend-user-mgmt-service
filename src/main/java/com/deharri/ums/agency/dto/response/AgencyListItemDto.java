package com.deharri.ums.agency.dto.response;

import com.deharri.ums.agency.entity.Agency;
import com.deharri.ums.enums.PakistanCity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgencyListItemDto {

    private String agencyId;

    private String agencyName;

    private PakistanCity city;

    private Agency.VerificationStatus verificationStatus;

    private Integer totalWorkers;

    private BigDecimal averageRating;

    private Integer totalJobsCompleted;

}
