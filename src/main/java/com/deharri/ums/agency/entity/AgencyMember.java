package com.deharri.ums.agency.entity;

import com.deharri.ums.base.TimeStampFields;
import com.deharri.ums.enums.AgencyRole;
import com.deharri.ums.user.entity.CoreUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agency_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"agency_id", "user_id"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AgencyMember extends TimeStampFields {

    @Id
    @Column(name = "member_id", updatable = false, nullable = false)
    private UUID memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", referencedColumnName = "agency_id", nullable = false)
    private Agency agency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private CoreUser coreUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgencyRole agencyRole;

    /** Timestamp the *current* stint began. Refreshed if the worker leaves and rejoins. */
    @Column
    private Instant joinedAt;

    /** Set when the membership ended (worker left, or was removed). Cleared on rejoin. */
    @Column
    private Instant leftAt;

    @Enumerated(EnumType.STRING)
    // columnDefinition: lets Hibernate add the column to existing tables cleanly
    // (it would otherwise refuse to add a NOT NULL column without a default).
    @Column(nullable = false, length = 16, columnDefinition = "VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'")
    @Builder.Default
    private MembershipStatus membershipStatus = MembershipStatus.ACTIVE;

    public enum MembershipStatus {
        /** Currently in the agency. */
        ACTIVE,
        /** Worker left voluntarily. */
        LEFT,
        /** Agency owner removed the worker. */
        REMOVED
    }

    @PrePersist
    protected void prePersist() {
        if (memberId == null) {
            memberId = UUID.randomUUID();
        }
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
        if (membershipStatus == null) {
            membershipStatus = MembershipStatus.ACTIVE;
        }
    }
}
