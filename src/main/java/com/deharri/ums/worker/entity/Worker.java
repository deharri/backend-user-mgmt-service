package com.deharri.ums.worker.entity;

import com.deharri.ums.base.TimeStampFields;
import com.deharri.ums.user.entity.CoreUser;
import com.deharri.ums.worker.dto.response.WorkerTypeDto;
import jakarta.persistence.*;
import lombok.*;

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
    @Column(updatable = false, nullable = false)
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

    private List<String> skills = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String bio; // HTML-aware rich text bio



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
