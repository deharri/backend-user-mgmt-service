package com.deharri.ums.auth;

import com.deharri.ums.auth.dto.request.LoginRequestDto;
import com.deharri.ums.auth.dto.request.RefreshTokenDto;
import com.deharri.ums.auth.dto.request.RegisterRequestDto;
import com.deharri.ums.auth.dto.response.AuthResponseDto;
import com.deharri.ums.auth.mapper.AuthMapper;
import com.deharri.ums.config.mail.EmailService;
import com.deharri.ums.config.security.jwt.refresh.RefreshToken;
import com.deharri.ums.config.security.jwt.refresh.RefreshTokenService;
import com.deharri.ums.enums.ExceptionMessage;
import com.deharri.ums.error.exception.AuthenticationException;
import com.deharri.ums.user.UserRepository;
import com.deharri.ums.user.entity.CoreUser;
import com.deharri.ums.user.entity.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private CoreUser coreUser;
    private AuthResponseDto authResponseDto;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        coreUser = CoreUser.builder()
                .userId(UUID.randomUUID())
                .username("john_doe")
                .password("encodedPassword123")
                .firstName("John")
                .lastName("Doe")
                .userData(UserData.builder()
                        .dataId(UUID.randomUUID())
                        .phoneNumber("+1234567890")
                        .build())
                .build();

        authResponseDto = AuthResponseDto.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .build();

        refreshToken = RefreshToken.builder()
                .id(1L)
                .token("valid-refresh-token")
                .username("john_doe")
                .expiryDate(new Date(System.currentTimeMillis() + 86400000))
                .build();
    }

    // ========================================================================
    // register() tests
    // ========================================================================

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("Should return auth response when registration data is valid")
        void givenValidRegistration_whenRegister_thenReturnAuthResponse() {
            // given
            RegisterRequestDto registerRequest = RegisterRequestDto.builder()
                    .phoneNumber("+1234567890")
                    .username("john_doe")
                    .password("SecureP@ss123")
                    .firstName("John")
                    .lastName("Doe")
                    .rememberMe(false)
                    .build();

            when(authMapper.registerRequestDtoToCoreUser(registerRequest)).thenReturn(coreUser);
            when(userRepository.save(coreUser)).thenReturn(coreUser);
            when(authMapper.coreUserToAuthResponseDto(coreUser, false)).thenReturn(authResponseDto);

            // when
            AuthResponseDto result = authService.register(registerRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("mock-access-token");
            assertThat(result.getRefreshToken()).isEqualTo("mock-refresh-token");

            verify(authMapper).registerRequestDtoToCoreUser(registerRequest);
            verify(userRepository).save(coreUser);
            verify(authMapper).coreUserToAuthResponseDto(coreUser, false);
        }

        @Test
        @DisplayName("Should pass rememberMe=false to mapper when rememberMe is false")
        void givenRememberMeFalse_whenRegister_thenPassRememberMeFalseToMapper() {
            // given
            RegisterRequestDto registerRequest = RegisterRequestDto.builder()
                    .phoneNumber("+1234567890")
                    .username("john_doe")
                    .password("SecureP@ss123")
                    .firstName("John")
                    .lastName("Doe")
                    .rememberMe(false)
                    .build();

            when(authMapper.registerRequestDtoToCoreUser(registerRequest)).thenReturn(coreUser);
            when(userRepository.save(coreUser)).thenReturn(coreUser);
            when(authMapper.coreUserToAuthResponseDto(coreUser, false)).thenReturn(authResponseDto);

            // when
            authService.register(registerRequest);

            // then
            verify(authMapper).coreUserToAuthResponseDto(eq(coreUser), eq(false));
        }
    }

    // ========================================================================
    // login() tests
    // ========================================================================

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Should return auth response when credentials are valid")
        void givenValidCredentials_whenLogin_thenReturnAuthResponse() {
            // given
            LoginRequestDto loginRequest = LoginRequestDto.builder()
                    .username("john_doe")
                    .password("SecureP@ss123")
                    .rememberMe(false)
                    .build();

            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(coreUser));
            when(passwordEncoder.matches("SecureP@ss123", coreUser.getPassword())).thenReturn(true);
            when(authMapper.coreUserToAuthResponseDto(coreUser, false)).thenReturn(authResponseDto);

            // when
            AuthResponseDto result = authService.login(loginRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("mock-access-token");
            assertThat(result.getRefreshToken()).isEqualTo("mock-refresh-token");

            verify(userRepository).findByUsername("john_doe");
            verify(passwordEncoder).matches("SecureP@ss123", coreUser.getPassword());
            verify(authMapper).coreUserToAuthResponseDto(coreUser, false);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when username does not exist")
        void givenInvalidUsername_whenLogin_thenThrowAuthenticationException() {
            // given
            LoginRequestDto loginRequest = LoginRequestDto.builder()
                    .username("nonexistent_user")
                    .password("SecureP@ss123")
                    .rememberMe(false)
                    .build();

            when(userRepository.findByUsername("nonexistent_user")).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage(ExceptionMessage.USER_NOT_FOUND_WITH_USERNAME.getText());

            verify(userRepository).findByUsername("nonexistent_user");
        }

        @Test
        @DisplayName("Should throw AuthenticationException when password is incorrect")
        void givenIncorrectPassword_whenLogin_thenThrowAuthenticationException() {
            // given
            LoginRequestDto loginRequest = LoginRequestDto.builder()
                    .username("john_doe")
                    .password("WrongPassword!")
                    .rememberMe(false)
                    .build();

            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(coreUser));
            when(passwordEncoder.matches("WrongPassword!", coreUser.getPassword())).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage(ExceptionMessage.INCORRECT_PASSWORD.getText());

            verify(userRepository).findByUsername("john_doe");
            verify(passwordEncoder).matches("WrongPassword!", coreUser.getPassword());
        }

        @Test
        @DisplayName("Should pass rememberMe=true to mapper when rememberMe is true")
        void givenRememberMeTrue_whenLogin_thenPassRememberMeToMapper() {
            // given
            LoginRequestDto loginRequest = LoginRequestDto.builder()
                    .username("john_doe")
                    .password("SecureP@ss123")
                    .rememberMe(true)
                    .build();

            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(coreUser));
            when(passwordEncoder.matches("SecureP@ss123", coreUser.getPassword())).thenReturn(true);
            when(authMapper.coreUserToAuthResponseDto(coreUser, true)).thenReturn(authResponseDto);

            // when
            authService.login(loginRequest);

            // then
            verify(authMapper).coreUserToAuthResponseDto(eq(coreUser), eq(true));
        }
    }

    // ========================================================================
    // refresh() tests
    // ========================================================================

    @Nested
    @DisplayName("refresh()")
    class RefreshTests {

        @Test
        @DisplayName("Should return auth response when refresh token is valid")
        void givenValidRefreshToken_whenRefresh_thenReturnAuthResponse() {
            // given
            RefreshTokenDto refreshTokenDto = RefreshTokenDto.builder()
                    .token("valid-refresh-token")
                    .build();

            when(refreshTokenService.getRefreshTokenIfExists("valid-refresh-token")).thenReturn(refreshToken);
            when(authMapper.refreshTokenToAuthResponseDto(refreshToken)).thenReturn(authResponseDto);

            // when
            AuthResponseDto result = authService.refresh(refreshTokenDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("mock-access-token");
            assertThat(result.getRefreshToken()).isEqualTo("mock-refresh-token");

            verify(refreshTokenService).getRefreshTokenIfExists("valid-refresh-token");
            verify(authMapper).refreshTokenToAuthResponseDto(refreshToken);
        }
    }

    // ========================================================================
    // logout() tests
    // ========================================================================

    @Nested
    @DisplayName("logout()")
    class LogoutTests {

        @Test
        @DisplayName("Should return success message and delete token when refresh token is valid")
        void givenValidRefreshToken_whenLogout_thenReturnSuccessMessage() {
            // given
            RefreshTokenDto refreshTokenDto = RefreshTokenDto.builder()
                    .token("valid-refresh-token")
                    .build();

            when(refreshTokenService.getRefreshTokenIfExists("valid-refresh-token")).thenReturn(refreshToken);

            // when
            Map<String, String> result = authService.logout(refreshTokenDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result).containsKey("message");
            assertThat(result.get("message")).isEqualTo("User: john_doe logged out successfully!");

            verify(refreshTokenService).getRefreshTokenIfExists("valid-refresh-token");
            verify(refreshTokenService).deleteToken(refreshToken);
        }
    }
}
