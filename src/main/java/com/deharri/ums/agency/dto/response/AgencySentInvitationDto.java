package com.deharri.ums.agency.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Invitation as seen by the AGENCY owner (outgoing direction). The mobile app's
 * {@code WorkerInvitationDto} is the worker's incoming view; this is the inverse.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgencySentInvitationDto {
    private UUID invitationId;
    /** Worker entity primary key (used by the {@code /workers/{id}/invite} path). */
    private UUID workerId;
    /** Worker's user UUID — the same id used by JWT and by job-service for dispatch. */
    private UUID workerUserId;
    private String workerUsername;
    private String workerFirstName;
    private String workerLastName;
    private String workerType;
    private String status;
    /** From TimeStampFields — LocalDateTime (no timezone). */
    private LocalDateTime createdAt;
    /** From the entity — Instant (UTC). */
    private Instant respondedAt;
}
