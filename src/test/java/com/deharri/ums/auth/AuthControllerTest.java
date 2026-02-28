package com.deharri.ums.auth;

import com.deharri.ums.auth.dto.request.LoginRequestDto;
import com.deharri.ums.auth.dto.request.RefreshTokenDto;
import com.deharri.ums.auth.dto.request.RegisterRequestDto;
import com.deharri.ums.auth.dto.response.AuthResponseDto;
import com.deharri.ums.config.security.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private AuthResponseDto authResponseDto;

    @BeforeEach
    void setUp() {
        authResponseDto = AuthResponseDto.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .build();
    }

    // ========================================================================
    // POST /api/v1/auth/register
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class RegisterEndpointTests {

        @Test
        @DisplayName("Should return 201 with tokens when registration request is valid")
        void givenValidRegistrationRequest_whenRegister_thenReturn201WithTokens() throws Exception {
            // given
            RegisterRequestDto registerRequest = RegisterRequestDto.builder()
                    .phoneNumber("+1234567890")
                    .username("john_doe")
                    .password("SecureP@ss123")
                    .firstName("John")
                    .lastName("Doe")
                    .rememberMe(false)
                    .build();

            when(authService.register(any(RegisterRequestDto.class))).thenReturn(authResponseDto);

            // when / then
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                    .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"));
        }
    }

    // ========================================================================
    // POST /api/v1/auth/login
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class LoginEndpointTests {

        @Test
        @DisplayName("Should return 200 with tokens when login request is valid")
        void givenValidLoginRequest_whenLogin_thenReturn200WithTokens() throws Exception {
            // given
            LoginRequestDto loginRequest = LoginRequestDto.builder()
                    .username("john_doe")
                    .password("SecureP@ss123")
                    .rememberMe(false)
                    .build();

            when(authService.login(any(LoginRequestDto.class))).thenReturn(authResponseDto);

            // when / then
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                    .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"));
        }
    }

    // ========================================================================
    // POST /api/v1/auth/refresh
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    class RefreshEndpointTests {

        @Test
        @DisplayName("Should return 200 with new tokens when refresh token is valid")
        void givenValidRefreshToken_whenRefresh_thenReturn200WithNewTokens() throws Exception {
            // given
            RefreshTokenDto refreshTokenDto = RefreshTokenDto.builder()
                    .token("valid-refresh-token")
                    .build();

            when(authService.refresh(any(RefreshTokenDto.class))).thenReturn(authResponseDto);

            // when / then
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshTokenDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                    .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"));
        }
    }

    // ========================================================================
    // POST /api/v1/auth/logout
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/auth/logout")
    class LogoutEndpointTests {

        @Test
        @DisplayName("Should return 200 with success message when refresh token is valid")
        void givenValidRefreshToken_whenLogout_thenReturn200WithMessage() throws Exception {
            // given
            RefreshTokenDto refreshTokenDto = RefreshTokenDto.builder()
                    .token("valid-refresh-token")
                    .build();

            Map<String, String> logoutResponse = Map.of("message", "User: john_doe logged out successfully!");
            when(authService.logout(any(RefreshTokenDto.class))).thenReturn(logoutResponse);

            // when / then
            mockMvc.perform(post("/api/v1/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshTokenDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("User: john_doe logged out successfully!"));
        }
    }
}
