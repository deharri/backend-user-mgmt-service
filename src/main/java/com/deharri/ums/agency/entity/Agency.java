package com.deharri.ums.agency.entity;

import com.deharri.ums.base.TimeStampFields;
import com.deharri.ums.enums.PakistanCity;
import com.deharri.ums.user.entity.CoreUser;
import com.deharri.ums.worker.entity.Worker;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Agency extends TimeStampFields {

    @Id
    @Column(name = "agency_id", updatable = false, nullable = false)
    private UUID agencyId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private CoreUser coreUser; // User account associated with the agency

    @Column(nullable = false, length = 200)
    private String agencyName; // Official name of the agency

    @Column(columnDefinition = "TEXT")
    private String description; // Description of the agency and services offered

    @Column(length = 20)
    private String contactNumber; // Primary contact number

    @Column(length = 100)
    private String contactEmail; // Primary contact email

    // Location
    @Enumerated(EnumType.STRING)
    private PakistanCity city; // City where agency is based

    private String address; // Physical address of the agency

    // Service Coverage
    @Builder.Default
    @ElementCollection(targetClass = PakistanCity.class)
    @CollectionTable(name = "agency_service_cities", joinColumns = @JoinColumn(name = "agency_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "city")
    private List<PakistanCity> serviceCities = new ArrayList<>(); // Cities where agency operates

    // Verification
    private String licenseNumber; // Business license number (optional for FYP)

    @Column(length = 500)
    private String licensePath; // S3 path for license document

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    // Workers managed by this agency
    @OneToMany(mappedBy = "agency", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Worker> workers = new ArrayList<>();

    // Agency Members (users with different roles in the agency)
    @OneToMany(mappedBy = "agency", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AgencyMember> members = new ArrayList<>();

    // Statistics
    @Builder.Default
    private Integer totalWorkers = 0; // Count of workers under this agency

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating; // Average rating across all agency workers

    @Builder.Default
    private Integer totalJobsCompleted = 0; // Total jobs completed by all agency workers

    @PrePersist
    protected void prePersist() {
        if (agencyId == null) {
            agencyId = UUID.randomUUID();
        }
        if (verificationStatus == null) {
            verificationStatus = VerificationStatus.PENDING;
        }
    }

    @AllArgsConstructor
    @Getter
    public enum VerificationStatus {
        PENDING("Pending Verification"),
        VERIFIED("Verified"),
        REJECTED("Rejected");

        private final String displayName;
    }
}
