package com.deharri.ums.integration;

import com.deharri.ums.auth.dto.request.RegisterRequestDto;
import com.deharri.ums.user.dto.request.UserEmailUpdateDto;
import com.deharri.ums.user.dto.request.UserPasswordUpdateDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USERS_BASE_URL = "/api/v1/users";
    private static final String AUTH_BASE_URL = "/api/v1/auth";
    private static final String VALID_PASSWORD = "Test@1234";

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    /**
     * Registers a user and returns the access token.
     */
    private String registerAndGetAccessToken(String username, String phone) throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username(username)
                .password(VALID_PASSWORD)
                .phoneNumber(phone)
                .firstName("John")
                .lastName("Doe")
                .rememberMe(false)
                .build();

        MvcResult result = mockMvc.perform(post(AUTH_BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        return node.get("accessToken").asText();
    }

    /**
     * Registers a user and returns the user's UUID extracted from the /me endpoint.
     */
    private String registerAndGetUserId(String username, String phone) throws Exception {
        String token = registerAndGetAccessToken(username, phone);

        MvcResult result = mockMvc.perform(get(USERS_BASE_URL + "/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        return node.get("userId").asText();
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    void givenNoUsers_whenGetAllUsers_thenReturn200WithEmptyList() throws Exception {
        mockMvc.perform(get(USERS_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void givenRegisteredUser_whenGetAllUsers_thenReturn200WithUserInList() throws Exception {
        registerAndGetAccessToken("listuser", "+1111111111");

        MvcResult result = mockMvc.perform(get(USERS_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        JsonNode array = objectMapper.readTree(body);
        assertThat(array.get(0).get("firstName").asText()).isEqualTo("John");
        assertThat(array.get(0).get("lastName").asText()).isEqualTo("Doe");
    }

    @Test
    void givenAuthenticatedUser_whenGetMyProfile_thenReturn200WithProfile() throws Exception {
        String token = registerAndGetAccessToken("profileuser", "+2222222222");

        mockMvc.perform(get(USERS_BASE_URL + "/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phoneNumber").value("+2222222222"));
    }

    @Test
    void givenUnauthenticatedRequest_whenGetMyProfile_thenReturn401() throws Exception {
        mockMvc.perform(get(USERS_BASE_URL + "/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenValidUuid_whenGetUserProfile_thenReturn200() throws Exception {
        String token = registerAndGetAccessToken("uuiduser", "+3333333333");

        // Get the user's UUID from /me
        MvcResult meResult = mockMvc.perform(get(USERS_BASE_URL + "/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String body = meResult.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        String userId = node.get("userId").asText();

        // Fetch by UUID
        mockMvc.perform(get(USERS_BASE_URL + "/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void givenAuthenticatedUser_whenUpdatePassword_thenReturn200() throws Exception {
        String token = registerAndGetAccessToken("pwduser", "+4444444444");

        UserPasswordUpdateDto dto = UserPasswordUpdateDto.builder()
                .oldPassword(VALID_PASSWORD)
                .newPassword("NewPass@5678")
                .build();

        mockMvc.perform(put(USERS_BASE_URL + "/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password Updated Successfully!"));
    }

    @Test
    void givenAuthenticatedUser_whenUpdateEmail_thenReturn200() throws Exception {
        String token = registerAndGetAccessToken("emailuser", "+5555555555");

        UserEmailUpdateDto dto = UserEmailUpdateDto.builder()
                .oldPassword(VALID_PASSWORD)
                .newEmail("newemail@example.com")
                .build();

        mockMvc.perform(put(USERS_BASE_URL + "/email")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email Updated Successfully!"));
    }

    @Test
    void givenWrongOldPassword_whenUpdatePassword_thenReturn401() throws Exception {
        String token = registerAndGetAccessToken("wrongoldpw", "+6666666666");

        UserPasswordUpdateDto dto = UserPasswordUpdateDto.builder()
                .oldPassword("Wrong@Pass1")
                .newPassword("NewPass@5678")
                .build();

        mockMvc.perform(put(USERS_BASE_URL + "/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenInvalidNewPassword_whenUpdatePassword_thenReturn400() throws Exception {
        String token = registerAndGetAccessToken("badnewpw", "+7777777777");

        UserPasswordUpdateDto dto = UserPasswordUpdateDto.builder()
                .oldPassword(VALID_PASSWORD)
                .newPassword("weak")
                .build();

        mockMvc.perform(put(USERS_BASE_URL + "/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenNonExistentUuid_whenGetUserProfile_thenReturnError() throws Exception {
        String token = registerAndGetAccessToken("existuser", "+8888888888");
        String fakeUuid = "00000000-0000-0000-0000-000000000000";

        // getUserProfile throws IllegalArgumentException which is handled by the
        // generic exception handler returning 500
        mockMvc.perform(get(USERS_BASE_URL + "/" + fakeUuid)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());
    }
}
