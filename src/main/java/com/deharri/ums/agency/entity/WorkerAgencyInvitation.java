package com.deharri.ums.agency.entity;

import com.deharri.ums.base.TimeStampFields;
import com.deharri.ums.worker.entity.Worker;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "worker_agency_invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerAgencyInvitation extends TimeStampFields {

    public enum Status { PENDING, ACCEPTED, REJECTED }

    @Id
    @Column(name = "invitation_id", updatable = false, nullable = false)
    private UUID invitationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    private Instant respondedAt;

    @PrePersist
    protected void prePersist() {
        if (invitationId == null) {
            invitationId = UUID.randomUUID();
        }
    }
}
