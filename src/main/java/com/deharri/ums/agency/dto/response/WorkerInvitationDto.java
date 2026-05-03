package com.deharri.ums.agency.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerInvitationDto {
    private UUID invitationId;
    private UUID agencyId;
    private String agencyName;
    private String status;
    private LocalDateTime createdAt;
}
