package com.deharri.ums.worker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@AllArgsConstructor @NoArgsConstructor
@Getter
@Setter
@Builder
public class CnicVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id", updatable = false, nullable = false)
    private long verificationId;

    @Enumerated(EnumType.STRING)
    private Status verificationStatus = Status.PENDING;

    private String cnic;

    private String cnicFrontPath = "";

    private String cnicBackPath = "";

    public enum Status {
        PENDING,
        VERIFIED,
        REJECTED
    }


}
