package com.deharri.ums.user.entity;

import com.deharri.ums.base.TimeStampFields;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "core_user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_core_user_username", columnNames = "username"),
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class CoreUser extends TimeStampFields {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID uuid;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_data_uuid", referencedColumnName = "uuid")
    private UserData userData;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @PrePersist
    private void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }
}
