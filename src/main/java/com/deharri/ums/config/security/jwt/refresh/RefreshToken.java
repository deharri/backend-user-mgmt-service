package com.deharri.ums.config.security.jwt.refresh;

import jakarta.persistence.*;
import jakarta.validation.Constraint;
import lombok.*;

import java.util.Date;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_refresh_token_token", columnNames = "token"),
                @UniqueConstraint(name = "uk_refresh_token_username", columnNames = "username")
        }
)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private Date expiryDate;

    public boolean isExpired() {
        return expiryDate.before(new Date());
    }

}

