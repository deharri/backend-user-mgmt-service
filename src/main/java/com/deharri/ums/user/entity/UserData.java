package com.deharri.ums.user.entity;

import com.deharri.ums.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "user_data",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_core_user_phone_number", columnNames = "phone_number")
        }
)
public class UserData {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID uuid;

    @Column(nullable = false)
    private String phoneNumber;

    private String email;

    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    private List<UserRole> userRoles;

    @Column(columnDefinition = "TEXT")
    private String bio; // HTML-aware rich text bio

    @PrePersist
    private void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        userRoles = new ArrayList<>();
        userRoles.add(UserRole.ROLE_CONSUMER);
    }
}
