package com.deharri.ums.agency.entity;

import com.deharri.ums.base.TimeStampFields;
import com.deharri.ums.enums.AgencyRole;
import com.deharri.ums.enums.RequestStatus;
import com.deharri.ums.enums.RequestType;
import com.deharri.ums.worker.entity.Worker;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "worker_agency_requests")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class WorkerAgencyRequest extends TimeStampFields {

    @Id
    @Column(name = "request_id", updatable = false, nullable = false)
    private UUID requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", referencedColumnName = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", referencedColumnName = "agency_id", nullable = false)
    private Agency agency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestType requestType; // WORKER_REQUEST or AGENCY_INVITATION

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING; // PENDING, ACCEPTED, REJECTED, CANCELLED

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AgencyRole proposedRole = AgencyRole.AGENCY_WORKER; // Role being offered/requested

    @Column(columnDefinition = "TEXT")
    private String message; // Optional message from requester/inviter

    private LocalDateTime respondedAt; // When the request was accepted/rejected

    @Column(length = 500)
    private String responseMessage; // Optional message when responding

    @PrePersist
    protected void prePersist() {
        if (requestId == null) {
            requestId = UUID.randomUUID();
        }
        if (status == null) {
            status = RequestStatus.PENDING;
        }
        if (proposedRole == null) {
            proposedRole = AgencyRole.AGENCY_WORKER;
        }
    }
}
