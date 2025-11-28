package com.deharri.ums.worker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Builder
public class AvailabilityStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "availability_id", updatable = false, nullable = false)
    private long availabilityId;

    @Enumerated(EnumType.STRING)
    private Status availabilityStatus;

    private LocalDateTime unavailableFrom;

    private LocalDateTime unavailableUntil;

    private String unavailabilityReason;


    public enum Status {
        AVAILABLE,
        UNAVAILABLE,
        BUSY
    }

}
