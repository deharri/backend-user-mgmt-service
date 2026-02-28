package com.deharri.ums.config.security.jwt.refresh;

import com.deharri.ums.config.security.jwt.JwtService;
import com.deharri.ums.enums.ExceptionMessage;
import com.deharri.ums.error.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Unit Tests")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private static final String TOKEN_STRING = "valid.refresh.token";
    private static final String USERNAME = "john_doe";

    private RefreshToken validRefreshToken;
    private RefreshToken expiredRefreshToken;

    @BeforeEach
    void setUp() {
        validRefreshToken = RefreshToken.builder()
                .id(1L)
                .token(TOKEN_STRING)
                .username(USERNAME)
                .expiryDate(new Date(System.currentTimeMillis() + 86400000)) // 1 day from now
                .build();

        expiredRefreshToken = RefreshToken.builder()
                .id(2L)
                .token("expired.refresh.token")
                .username(USERNAME)
                .expiryDate(new Date(System.currentTimeMillis() - 86400000)) // 1 day ago
                .build();
    }

    // ========================================================================
    // getRefreshTokenIfExists() tests
    // ========================================================================

    @Nested
    @DisplayName("getRefreshTokenIfExists()")
    class GetRefreshTokenIfExistsTests {

        @Test
        @DisplayName("Should return refresh token when token exists and is valid")
        void givenExistingToken_whenGetRefreshTokenIfExists_thenReturnToken() {
            // given
            when(refreshTokenRepository.findByToken(TOKEN_STRING)).thenReturn(Optional.of(validRefreshToken));
            when(jwtService.extractUsernameFromToken(TOKEN_STRING)).thenReturn(USERNAME);

            // when
            RefreshToken result = refreshTokenService.getRefreshTokenIfExists(TOKEN_STRING);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo(TOKEN_STRING);
            assertThat(result.getUsername()).isEqualTo(USERNAME);
            verify(refreshTokenRepository).findByToken(TOKEN_STRING);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when token does not exist")
        void givenNonExistingToken_whenGetRefreshTokenIfExists_thenThrowAuthenticationException() {
            // given
            when(refreshTokenRepository.findByToken("nonexistent.token")).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> refreshTokenService.getRefreshTokenIfExists("nonexistent.token"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage(ExceptionMessage.REFRESH_TOKEN_NOT_FOUND.getText());

            verify(refreshTokenRepository).findByToken("nonexistent.token");
        }
    }

    // ========================================================================
    // nonExpiredAndValidRefreshToken() tests
    // ========================================================================

    @Nested
    @DisplayName("nonExpiredAndValidRefreshToken()")
    class NonExpiredAndValidRefreshTokenTests {

        @Test
        @DisplayName("Should delete token and throw when token is expired")
        void givenExpiredToken_whenNonExpiredAndValidRefreshToken_thenDeleteAndThrow() {
            // given / when / then
            assertThatThrownBy(() -> refreshTokenService.nonExpiredAndValidRefreshToken(expiredRefreshToken))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage(ExceptionMessage.REFRESH_TOKEN_EXPIRED.getText());

            verify(refreshTokenRepository).delete(expiredRefreshToken);
        }

        @Test
        @DisplayName("Should delete token and throw when extracted username does not match stored username")
        void givenUsernameMismatch_whenNonExpiredAndValidRefreshToken_thenDeleteAndThrow() {
            // given
            when(jwtService.extractUsernameFromToken(TOKEN_STRING)).thenReturn("different_user");

            // when / then
            assertThatThrownBy(() -> refreshTokenService.nonExpiredAndValidRefreshToken(validRefreshToken))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage(ExceptionMessage.REFRESH_TOKEN_EXPIRED.getText());

            verify(refreshTokenRepository).delete(validRefreshToken);
        }

        @Test
        @DisplayName("Should return token when token is valid and not expired")
        void givenValidToken_whenNonExpiredAndValidRefreshToken_thenReturnToken() {
            // given
            when(jwtService.extractUsernameFromToken(TOKEN_STRING)).thenReturn(USERNAME);

            // when
            RefreshToken result = refreshTokenService.nonExpiredAndValidRefreshToken(validRefreshToken);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(validRefreshToken);
            verify(refreshTokenRepository, never()).delete(any());
        }
    }

    // ========================================================================
    // deleteToken() tests
    // ========================================================================

    @Nested
    @DisplayName("deleteToken()")
    class DeleteTokenTests {

        @Test
        @DisplayName("Should call repository delete when given a refresh token")
        void givenRefreshToken_whenDeleteToken_thenCallRepositoryDelete() {
            // given / when
            refreshTokenService.deleteToken(validRefreshToken);

            // then
            verify(refreshTokenRepository).delete(validRefreshToken);
        }
    }

    // ========================================================================
    // saveToken() tests
    // ========================================================================

    @Nested
    @DisplayName("saveToken()")
    class SaveTokenTests {

        @Test
        @DisplayName("Should update existing token when username already has a refresh token")
        void givenExistingUsername_whenSaveToken_thenUpdateExistingToken() {
            // given
            String newTokenString = "new.refresh.token";
            Date newExpiry = new Date(System.currentTimeMillis() + 172800000); // 2 days from now

            RefreshToken existingToken = RefreshToken.builder()
                    .id(1L)
                    .token("old.refresh.token")
                    .username(USERNAME)
                    .expiryDate(new Date(System.currentTimeMillis() + 86400000))
                    .build();

            when(refreshTokenRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingToken));
            when(jwtService.extractExpiration(newTokenString)).thenReturn(newExpiry);

            // when
            refreshTokenService.saveToken(newTokenString, USERNAME);

            // then
            verify(refreshTokenRepository).findByUsername(USERNAME);
            verify(jwtService).extractExpiration(newTokenString);
            verify(refreshTokenRepository).save(existingToken);
            assertThat(existingToken.getToken()).isEqualTo(newTokenString);
            assertThat(existingToken.getUsername()).isEqualTo(USERNAME);
            assertThat(existingToken.getExpiryDate()).isEqualTo(newExpiry);
        }

        @Test
        @DisplayName("Should create new token when username does not have an existing refresh token")
        void givenNewUsername_whenSaveToken_thenCreateNewToken() {
            // given
            String newTokenString = "brand.new.token";
            String newUsername = "new_user";
            Date newExpiry = new Date(System.currentTimeMillis() + 172800000);

            when(refreshTokenRepository.findByUsername(newUsername)).thenReturn(Optional.empty());
            when(jwtService.extractExpiration(newTokenString)).thenReturn(newExpiry);

            // when
            refreshTokenService.saveToken(newTokenString, newUsername);

            // then
            verify(refreshTokenRepository).findByUsername(newUsername);
            verify(jwtService).extractExpiration(newTokenString);
            verify(refreshTokenRepository).save(argThat(token ->
                    token.getToken().equals(newTokenString) &&
                    token.getUsername().equals(newUsername) &&
                    token.getExpiryDate().equals(newExpiry)
            ));
        }
    }
}
