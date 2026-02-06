package com.deharri.ums.agency.entity;

import com.deharri.ums.base.TimeStampFields;
import com.deharri.ums.enums.AgencyRole;
import com.deharri.ums.user.entity.CoreUser;
import jakarta.persistence.*;
import lombok.*;

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

    @PrePersist
    protected void prePersist() {
        if (memberId == null) {
            memberId = UUID.randomUUID();
        }
    }
}
