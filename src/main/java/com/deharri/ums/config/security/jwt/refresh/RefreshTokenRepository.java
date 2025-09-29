package com.deharri.ums.config.security.jwt.refresh;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {


    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndUsername(String token, String username);

    Optional<RefreshToken> findByUsername(String username);
}
