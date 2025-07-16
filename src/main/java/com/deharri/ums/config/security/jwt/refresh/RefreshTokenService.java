package com.deharri.ums.config.security.jwt.refresh;

import com.deharri.ums.config.security.jwt.JwtService;
import com.deharri.ums.enums.ExceptionMessage;
import com.deharri.ums.error.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtService jwtService;

    public RefreshToken getRefreshTokenIfExists(String tokenString) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new AuthenticationException(ExceptionMessage.REFRESH_TOKEN_NOT_FOUND));

        return nonExpiredAndValidRefreshToken(refreshToken);
    }

    public RefreshToken nonExpiredAndValidRefreshToken(RefreshToken refreshToken) {
        if (refreshToken.isExpired() || !jwtService.extractUsernameFromToken(refreshToken.getToken()).equals(refreshToken.getUsername())) {
            deleteToken(refreshToken);
            throw new AuthenticationException(ExceptionMessage.REFRESH_TOKEN_EXPIRED);
        }
        return refreshToken;
    }

    public void deleteToken(RefreshToken refreshToken) {
        log.info("Deleting refresh token {}", refreshToken.getToken());
        refreshTokenRepository.delete(refreshToken);
    }


    @Transactional
    public void saveToken(String tokenString, String username) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByUsername(username)
                .orElse(new RefreshToken());

        refreshToken.setUsername(username);
        refreshToken.setToken(tokenString);
        refreshToken.setExpiryDate(jwtService.extractExpiration(tokenString));

        refreshTokenRepository.save(refreshToken);
    }

}