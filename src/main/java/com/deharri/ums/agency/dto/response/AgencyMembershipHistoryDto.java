package com.deharri.ums.agency.dto.response;

import com.deharri.ums.agency.entity.AgencyMember.MembershipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Used by the agency-owner "members history" view: lists every (agency, user) membership
 * — current and past — with timestamps and final status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgencyMembershipHistoryDto {
    private UUID memberId;
    private UUID userId;
    private UUID workerId;          // Worker entity PK if a worker profile still exists
    private String username;
    private String firstName;
    private String lastName;
    private String workerType;       // null if user has no current worker profile
    private MembershipStatus membershipStatus;
    private Instant joinedAt;
    private Instant leftAt;          // null while ACTIVE
}
