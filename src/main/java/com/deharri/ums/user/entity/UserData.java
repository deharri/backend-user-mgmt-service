package com.deharri.ums.user.entity;

import com.deharri.ums.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class UserData {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID uuid;

    private String email;

    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @PrePersist
    private void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }
}
