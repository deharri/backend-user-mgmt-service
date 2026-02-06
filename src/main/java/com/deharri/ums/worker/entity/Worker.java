package com.deharri.ums.worker.entity;

import com.deharri.ums.agency.entity.Agency;
import com.deharri.ums.base.TimeStampFields;
import com.deharri.ums.enums.Language;
import com.deharri.ums.enums.PakistanCity;
import com.deharri.ums.user.entity.CoreUser;
import com.deharri.ums.worker.dto.response.WorkerTypeDto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor @AllArgsConstructor
@Builder
@Getter
@Setter
public class Worker extends TimeStampFields {

    @Id
    @Column(name = "worker_id", updatable = false, nullable = false)
    private UUID workerId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private CoreUser coreUser;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "availability_status_id", referencedColumnName = "availability_id")
    private AvailabilityStatus availabilityStatus;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "verification_id", referencedColumnName = "verification_id")
    private CnicVerification cnicVerification;

    @Enumerated(EnumType.STRING)
    private WorkerType workerType;

    @Builder.Default
    private List<String> skills = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String bio; // HTML-aware rich text bio

    // Experience and Pricing
    private Integer experienceYears; // Years of experience in the field

    @Column(precision = 10, scale = 2)
    private BigDecimal hourlyRate; // Hourly rate in PKR

    @Column(precision = 10, scale = 2)
    private BigDecimal dailyRate; // Daily rate in PKR (optional, some workers prefer daily)

    // Location Information
    @Enumerated(EnumType.STRING)
    private PakistanCity city; // Primary city of operation

    private String area; // Specific area/locality within the city

    @Builder.Default
    @ElementCollection(targetClass = PakistanCity.class)
    @CollectionTable(name = "worker_service_cities", joinColumns = @JoinColumn(name = "worker_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "city")
    private List<PakistanCity> serviceCities = new ArrayList<>(); // Cities where worker provides services

    // Languages
    @Builder.Default
    @ElementCollection(targetClass = Language.class)
    @CollectionTable(name = "worker_languages", joinColumns = @JoinColumn(name = "worker_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "language")
    private List<Language> languages = new ArrayList<>(); // Languages spoken by the worker

    // Portfolio
    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "worker_portfolio", joinColumns = @JoinColumn(name = "worker_id"))
    @Column(name = "image_path")
    private List<String> portfolioImagePaths = new ArrayList<>(); // S3 paths for portfolio images

    // Agency Relationship (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", referencedColumnName = "agency_id")
    private Agency agency; // Worker can optionally belong to an agency

    // Statistics (updated by other services)
    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating; // Average rating from reviews (0.00 to 5.00)

    @Builder.Default
    private Integer totalJobsCompleted = 0; // Total number of jobs completed

    // Verification status helper
    @Transient
    public boolean isVerified() {
        return cnicVerification != null
                && cnicVerification.getVerificationStatus() == CnicVerification.Status.VERIFIED;
    }

    @PrePersist
    protected void prePersist() {
        if (workerId == null) {
            workerId = UUID.randomUUID();
        }
        availabilityStatus = AvailabilityStatus.builder()
                .availabilityStatus(AvailabilityStatus.Status.AVAILABLE)
                .unavailableFrom(null)
                .unavailableUntil(null)
                .unavailabilityReason(null)
                .build();
        cnicVerification = CnicVerification.builder()
                .verificationStatus(CnicVerification.Status.PENDING)
                .cnic(null)
                .cnicFrontPath(null)
                .cnicBackPath(null)
                .build();
    }

    @AllArgsConstructor
    @Getter
    public enum WorkerType {
        MECHANIC("Mechanic", "Performs mechanical repairs and maintenance"),
        ELECTRICIAN("Electrician", "Handles electrical installations and repairs"),
        PLUMBER("Plumber", "Manages plumbing systems and fixtures"),
        CARPENTER("Carpenter", "Specializes in woodwork and construction"),
        WELDER("Welder", "Performs welding and metal fabrication"),
        PAINTER("Painter", "Handles painting and surface finishing"),
        MASON("Mason", "Works with brick, stone, and concrete"),
        HVAC_TECHNICIAN("HVAC Technician", "Maintains heating, ventilation, and air conditioning systems"),
        GENERAL_LABORER("General Laborer", "Performs general construction and maintenance tasks");

        private final String displayName;
        private final String description;

        public WorkerTypeDto toDTO() {
            return new WorkerTypeDto(this.name(), this.displayName, this.description);
        }

        public static List<WorkerTypeDto> getAllTypes() {
            return Arrays.stream(WorkerType.values())
                    .map(WorkerType::toDTO)
                    .toList();
        }
    }

}
