package com.deharri.ums.user.entity;

import com.deharri.ums.base.TimeStampFields;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
    @Column(name = "user_id", updatable = false, nullable = false)
    protected UUID userId;

    @Column(nullable = false)
    protected String username;

    @Column(nullable = false)
    protected String password;

    @Column(nullable = false)
    protected String firstName;

    @Column(nullable = false)
    protected String lastName;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_data_id", referencedColumnName = "data_id")
    protected UserData userData;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @PrePersist
    protected void prePersist() {
        if (userId == null) {
            userId = UUID.randomUUID();
        }
    }
}
