package com.deharri.ums.integration;

import com.deharri.ums.auth.dto.request.LoginRequestDto;
import com.deharri.ums.auth.dto.request.RefreshTokenDto;
import com.deharri.ums.auth.dto.request.RegisterRequestDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String AUTH_BASE_URL = "/api/v1/auth";
    private static final String VALID_PASSWORD = "Test@1234";
    private static final String VALID_PHONE = "+1234567890";

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    private RegisterRequestDto buildRegisterRequest(String username, String phone) {
        return RegisterRequestDto.builder()
                .username(username)
                .password(VALID_PASSWORD)
                .phoneNumber(phone)
                .firstName("John")
                .lastName("Doe")
                .rememberMe(false)
                .build();
    }

    /**
     * Registers a user via the API and returns the raw MvcResult so callers
     * can extract tokens or inspect the response.
     */
    private MvcResult registerUser(String username, String phone) throws Exception {
        RegisterRequestDto request = buildRegisterRequest(username, phone);
        return mockMvc.perform(post(AUTH_BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private String extractField(MvcResult result, String fieldName) throws Exception {
        String body = result.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        return node.get(fieldName).asText();
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    void givenValidRegistration_whenRegister_thenReturn201WithTokens() throws Exception {
        RegisterRequestDto request = buildRegisterRequest("testuser", VALID_PHONE);

        mockMvc.perform(post(AUTH_BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void givenDuplicateUsername_whenRegister_thenReturn409Conflict() throws Exception {
        // First registration succeeds
        registerUser("duplicate_user", "+1111111111");

        // Second registration with same username should fail with 409
        RegisterRequestDto duplicateRequest = buildRegisterRequest("duplicate_user", "+2222222222");

        mockMvc.perform(post(AUTH_BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void givenRegisteredUser_whenLoginWithCorrectPassword_thenReturn200WithTokens() throws Exception {
        registerUser("loginuser", "+3333333333");

        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .username("loginuser")
                .password(VALID_PASSWORD)
                .rememberMe(false)
                .build();

        mockMvc.perform(post(AUTH_BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void givenRegisteredUser_whenLoginWithWrongPassword_thenReturn401() throws Exception {
        registerUser("wrongpwuser", "+4444444444");

        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .username("wrongpwuser")
                .password("WrongP@ss999")
                .rememberMe(false)
                .build();

        mockMvc.perform(post(AUTH_BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenNonExistentUsername_whenLogin_thenReturn401() throws Exception {
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .username("ghost_user")
                .password(VALID_PASSWORD)
                .rememberMe(false)
                .build();

        mockMvc.perform(post(AUTH_BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenValidRefreshToken_whenRefresh_thenReturn200WithNewTokens() throws Exception {
        MvcResult registerResult = registerUser("refreshuser", "+5555555555");
        String refreshToken = extractField(registerResult, "refreshToken");

        assertThat(refreshToken).isNotBlank();

        RefreshTokenDto refreshRequest = RefreshTokenDto.builder()
                .token(refreshToken)
                .build();

        mockMvc.perform(post(AUTH_BASE_URL + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void givenValidRefreshToken_whenLogout_thenReturn200WithMessage() throws Exception {
        MvcResult registerResult = registerUser("logoutuser", "+6666666666");
        String refreshToken = extractField(registerResult, "refreshToken");

        assertThat(refreshToken).isNotBlank();

        RefreshTokenDto logoutRequest = RefreshTokenDto.builder()
                .token(refreshToken)
                .build();

        MvcResult logoutResult = mockMvc.perform(post(AUTH_BASE_URL + "/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String body = logoutResult.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        assertThat(node.get("message").asText()).contains("logged out successfully");
    }

    @Test
    void givenInvalidRefreshToken_whenRefresh_thenReturn401() throws Exception {
        RefreshTokenDto refreshRequest = RefreshTokenDto.builder()
                .token("invalid-token-that-does-not-exist")
                .build();

        mockMvc.perform(post(AUTH_BASE_URL + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenInvalidPasswordFormat_whenRegister_thenReturn400() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("badpwuser")
                .password("weak")
                .phoneNumber("+7777777777")
                .firstName("Jane")
                .lastName("Doe")
                .rememberMe(false)
                .build();

        mockMvc.perform(post(AUTH_BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenInvalidUsernameFormat_whenRegister_thenReturn400() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("ab")
                .password(VALID_PASSWORD)
                .phoneNumber("+8888888888")
                .firstName("Jane")
                .lastName("Doe")
                .rememberMe(false)
                .build();

        mockMvc.perform(post(AUTH_BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
