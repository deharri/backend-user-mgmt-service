package com.deharri.ums.integration;

import com.deharri.ums.auth.dto.request.RegisterRequestDto;
import com.deharri.ums.worker.dto.request.CreateWorkerAccountDto;
import com.deharri.ums.worker.entity.Worker;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WorkerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String WORKERS_BASE_URL = "/api/v1/workers";
    private static final String WORKERS_PUBLIC_URL = "/public/api/v1/workers";
    private static final String AUTH_BASE_URL = "/api/v1/auth";
    private static final String VALID_PASSWORD = "Test@1234";

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

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

    private CreateWorkerAccountDto buildCreateWorkerDto() {
        return CreateWorkerAccountDto.builder()
                .workerType(Worker.WorkerType.ELECTRICIAN)
                .skills(List.of("Wiring", "Circuit repair"))
                .cnic("12345-1234567-1")
                .bio("Experienced electrician")
                .experienceYears(5)
                .build();
    }

    private void registerAndCreateWorker(String username, String phone) throws Exception {
        String token = registerAndGetAccessToken(username, phone);
        CreateWorkerAccountDto workerDto = buildCreateWorkerDto();

        mockMvc.perform(post(WORKERS_BASE_URL + "/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workerDto)))
                .andExpect(status().isCreated());
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    void givenPublicEndpoint_whenGetAllWorkerTypes_thenReturn200() throws Exception {
        mockMvc.perform(get(WORKERS_PUBLIC_URL + "/types/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(Worker.WorkerType.values().length))
                .andExpect(jsonPath("$[0].enumValue").isNotEmpty())
                .andExpect(jsonPath("$[0].displayName").isNotEmpty())
                .andExpect(jsonPath("$[0].description").isNotEmpty());
    }

    @Test
    void givenAuthenticatedUser_whenCreateWorkerAccount_thenReturn201() throws Exception {
        String token = registerAndGetAccessToken("workeruser", "+1111111111");

        CreateWorkerAccountDto dto = buildCreateWorkerDto();

        mockMvc.perform(post(WORKERS_BASE_URL + "/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Worker account created successfully"));
    }

    @Test
    void givenWorkerExists_whenGetAllWorkers_thenReturn200WithList() throws Exception {
        String token = registerAndGetAccessToken("listworker", "+2222222222");
        CreateWorkerAccountDto dto = buildCreateWorkerDto();

        mockMvc.perform(post(WORKERS_BASE_URL + "/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(WORKERS_BASE_URL)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void givenWorkerExists_whenGetMyWorkerProfile_thenReturn200() throws Exception {
        String token = registerAndGetAccessToken("myworker", "+3333333333");
        CreateWorkerAccountDto dto = buildCreateWorkerDto();

        mockMvc.perform(post(WORKERS_BASE_URL + "/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(WORKERS_BASE_URL + "/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workerType").value("ELECTRICIAN"))
                .andExpect(jsonPath("$.skills").isArray())
                .andExpect(jsonPath("$.skills.length()").value(2));
    }

    @Test
    void givenUnauthenticatedRequest_whenCreateWorkerAccount_thenReturn401() throws Exception {
        CreateWorkerAccountDto dto = buildCreateWorkerDto();

        mockMvc.perform(post(WORKERS_BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenInvalidCnicFormat_whenCreateWorkerAccount_thenReturn400() throws Exception {
        String token = registerAndGetAccessToken("badcnic", "+4444444444");

        CreateWorkerAccountDto dto = CreateWorkerAccountDto.builder()
                .workerType(Worker.WorkerType.PLUMBER)
                .skills(List.of("Pipe fitting"))
                .cnic("invalid-cnic")
                .build();

        mockMvc.perform(post(WORKERS_BASE_URL + "/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenMissingSkills_whenCreateWorkerAccount_thenReturn400() throws Exception {
        String token = registerAndGetAccessToken("noskills", "+5555555555");

        CreateWorkerAccountDto dto = CreateWorkerAccountDto.builder()
                .workerType(Worker.WorkerType.CARPENTER)
                .skills(List.of())
                .cnic("12345-1234567-1")
                .build();

        mockMvc.perform(post(WORKERS_BASE_URL + "/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenWorkerExists_whenGetWorkerById_thenReturn200() throws Exception {
        String token = registerAndGetAccessToken("findworker", "+6666666666");
        CreateWorkerAccountDto dto = buildCreateWorkerDto();

        mockMvc.perform(post(WORKERS_BASE_URL + "/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Get worker profile from /me to extract workerId
        MvcResult meResult = mockMvc.perform(get(WORKERS_BASE_URL + "/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String body = meResult.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        String workerId = node.get("workerId").asText();

        // Get worker by ID
        mockMvc.perform(get(WORKERS_BASE_URL + "/" + workerId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workerId").value(workerId))
                .andExpect(jsonPath("$.workerType").value("ELECTRICIAN"));
    }
}
