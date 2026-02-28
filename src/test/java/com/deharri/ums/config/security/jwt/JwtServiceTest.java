package com.deharri.ums.config.security.jwt;

import com.deharri.ums.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private static final String TEST_SECRET_KEY = "dGVzdFNlY3JldEtleUZvclVuaXRUZXN0aW5nUHVycG9zZXNPbmx5MTIzNDU2Nzg5MA==";
    private static final String TEST_USERNAME = "john_doe";
    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final List<UserRole> TEST_ROLES = List.of(UserRole.ROLE_CONSUMER);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", TEST_SECRET_KEY);
    }

    // ========================================================================
    // generateAccessToken() tests
    // ========================================================================

    @Nested
    @DisplayName("generateAccessToken()")
    class GenerateAccessTokenTests {

        @Test
        @DisplayName("Should return non-null token when given valid inputs")
        void givenValidInputs_whenGenerateAccessToken_thenReturnNonNullToken() {
            // given
            UUID userId = TEST_USER_ID;
            String username = TEST_USERNAME;
            List<UserRole> roles = TEST_ROLES;

            // when
            String token = jwtService.generateAccessToken(userId, username, roles);

            // then
            assertThat(token).isNotNull();
            assertThat(token).isNotBlank();
        }
    }

    // ========================================================================
    // extractUsernameFromToken() tests
    // ========================================================================

    @Nested
    @DisplayName("extractUsernameFromToken()")
    class ExtractUsernameTests {

        @Test
        @DisplayName("Should return correct username when given a valid token")
        void givenValidToken_whenExtractUsername_thenReturnCorrectUsername() {
            // given
            String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLES);

            // when
            String extractedUsername = jwtService.extractUsernameFromToken(token);

            // then
            assertThat(extractedUsername).isEqualTo(TEST_USERNAME);
        }

        @Test
        @DisplayName("Should return subject from access token")
        void givenAccessToken_whenExtractUsername_thenReturnSubject() {
            // given
            String expectedUsername = "test_subject_user";
            String token = jwtService.generateAccessToken(TEST_USER_ID, expectedUsername, TEST_ROLES);

            // when
            String extractedUsername = jwtService.extractUsernameFromToken(token);

            // then
            assertThat(extractedUsername).isEqualTo(expectedUsername);
        }
    }

    // ========================================================================
    // isTokenExpired() tests
    // ========================================================================

    @Nested
    @DisplayName("isTokenExpired()")
    class IsTokenExpiredTests {

        @Test
        @DisplayName("Should return false when token is not expired")
        void givenValidToken_whenIsTokenExpired_thenReturnFalse() {
            // given
            String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLES);

            // when
            boolean expired = jwtService.isTokenExpired(token);

            // then
            assertThat(expired).isFalse();
        }
    }

    // ========================================================================
    // isTokenValid() tests
    // ========================================================================

    @Nested
    @DisplayName("isTokenValid()")
    class IsTokenValidTests {

        @Test
        @DisplayName("Should return true when username matches and token is not expired")
        void givenValidTokenAndMatchingUser_whenIsTokenValid_thenReturnTrue() {
            // given
            String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLES);
            UserDetails userDetails = new User(TEST_USERNAME, "password", Collections.emptyList());

            // when
            boolean valid = jwtService.isTokenValid(TEST_USERNAME, userDetails, token);

            // then
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("Should return false when username does not match UserDetails")
        void givenValidTokenAndDifferentUser_whenIsTokenValid_thenReturnFalse() {
            // given
            String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLES);
            UserDetails userDetails = new User("different_user", "password", Collections.emptyList());

            // when
            boolean valid = jwtService.isTokenValid(TEST_USERNAME, userDetails, token);

            // then
            assertThat(valid).isFalse();
        }
    }

    // ========================================================================
    // generateRefreshToken() tests
    // ========================================================================

    @Nested
    @DisplayName("generateRefreshToken()")
    class GenerateRefreshTokenTests {

        @Test
        @DisplayName("Should generate token with 8-week expiry when rememberMe is true")
        void givenRememberMeTrue_whenGenerateRefreshToken_thenLongerExpiry() {
            // given
            boolean rememberMe = true;

            // when
            String token = jwtService.generateRefreshToken(TEST_USERNAME, rememberMe);
            Date expiration = jwtService.extractExpiration(token);

            // then
            assertThat(token).isNotNull().isNotBlank();

            long eightWeeksMillis = 1000L * 60 * 60 * 24 * 7 * 8;
            long expectedMinExpiry = System.currentTimeMillis() + eightWeeksMillis - 5000;
            long expectedMaxExpiry = System.currentTimeMillis() + eightWeeksMillis + 5000;

            assertThat(expiration.getTime()).isBetween(expectedMinExpiry, expectedMaxExpiry);
        }

        @Test
        @DisplayName("Should generate token with 1-week expiry when rememberMe is false")
        void givenRememberMeFalse_whenGenerateRefreshToken_thenShorterExpiry() {
            // given
            boolean rememberMe = false;

            // when
            String token = jwtService.generateRefreshToken(TEST_USERNAME, rememberMe);
            Date expiration = jwtService.extractExpiration(token);

            // then
            assertThat(token).isNotNull().isNotBlank();

            long oneWeekMillis = 1000L * 60 * 60 * 24 * 7;
            long expectedMinExpiry = System.currentTimeMillis() + oneWeekMillis - 5000;
            long expectedMaxExpiry = System.currentTimeMillis() + oneWeekMillis + 5000;

            assertThat(expiration.getTime()).isBetween(expectedMinExpiry, expectedMaxExpiry);
        }
    }

    // ========================================================================
    // extractExpiration() tests
    // ========================================================================

    @Nested
    @DisplayName("extractExpiration()")
    class ExtractExpirationTests {

        @Test
        @DisplayName("Should return a future date when token is valid")
        void givenValidToken_whenExtractExpiration_thenReturnFutureDate() {
            // given
            String token = jwtService.generateAccessToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLES);

            // when
            Date expiration = jwtService.extractExpiration(token);

            // then
            assertThat(expiration).isNotNull();
            assertThat(expiration).isAfter(new Date());
        }
    }
}
