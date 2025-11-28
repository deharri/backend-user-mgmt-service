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
    @Column(name = "data_id", updatable = false, nullable = false)
    private UUID dataId;

    @Column(nullable = false)
    private String phoneNumber;

    private String email;

    private String profilePicturePath;

    @Enumerated(EnumType.STRING)
    private List<UserRole> userRoles;


    @PrePersist
    private void prePersist() {
        if (dataId == null) {
            dataId = UUID.randomUUID();
        }
        userRoles = new ArrayList<>();
        userRoles.add(UserRole.ROLE_CONSUMER);
    }
}
