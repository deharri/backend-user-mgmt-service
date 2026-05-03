package com.deharri.ums.agency.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalAgencyDto {
    private UUID agencyId;
    private String agencyName;
    private UUID ownerUserId;
    private String ownerUsername;
    /** True iff agency.subscriptionStatus == ACTIVE AND not past its expiry. */
    private boolean subscriptionActive;
}
