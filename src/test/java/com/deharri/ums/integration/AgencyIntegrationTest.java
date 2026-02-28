package com.deharri.ums.integration;

import com.deharri.ums.agency.dto.request.CreateAgencyDto;
import com.deharri.ums.auth.dto.request.RegisterRequestDto;
import com.deharri.ums.enums.PakistanCity;
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
class AgencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String AGENCIES_BASE_URL = "/api/v1/agencies";
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

    private CreateAgencyDto buildCreateAgencyDto(String agencyName) {
        return CreateAgencyDto.builder()
                .agencyName(agencyName)
                .description("A professional agency")
                .contactNumber("+923001234567")
                .contactEmail("agency@example.com")
                .city(PakistanCity.LAHORE)
                .address("123 Main Street, Gulberg III, Lahore")
                .serviceCities(List.of(PakistanCity.LAHORE, PakistanCity.ISLAMABAD))
                .licenseNumber("LIC-12345")
                .build();
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    void givenAuthenticatedUser_whenCreateAgency_thenReturn201() throws Exception {
        String token = registerAndGetAccessToken("agencyowner", "+1111111111");

        CreateAgencyDto dto = buildCreateAgencyDto("Test Agency");

        mockMvc.perform(post(AGENCIES_BASE_URL + "/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Agency created successfully. You are now the agency admin."));
    }

    @Test
    void givenAgencyExists_whenGetAllAgencies_thenReturn200WithList() throws Exception {
        String token = registerAndGetAccessToken("agencylist", "+2222222222");

        CreateAgencyDto dto = buildCreateAgencyDto("List Agency");
        mockMvc.perform(post(AGENCIES_BASE_URL + "/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(AGENCIES_BASE_URL)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].agencyName").value("List Agency"))
                .andExpect(jsonPath("$[0].city").value("LAHORE"));
    }

    @Test
    void givenAgencyOwner_whenGetMyAgencyProfile_thenReturn200() throws Exception {
        String token = registerAndGetAccessToken("myagency", "+3333333333");

        CreateAgencyDto dto = buildCreateAgencyDto("My Agency");
        mockMvc.perform(post(AGENCIES_BASE_URL + "/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(AGENCIES_BASE_URL + "/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agencyName").value("My Agency"))
                .andExpect(jsonPath("$.city").value("LAHORE"))
                .andExpect(jsonPath("$.address").value("123 Main Street, Gulberg III, Lahore"))
                .andExpect(jsonPath("$.contactNumber").value("+923001234567"))
                .andExpect(jsonPath("$.licenseNumber").value("LIC-12345"));
    }

    @Test
    void givenUnauthenticatedRequest_whenCreateAgency_thenReturn401() throws Exception {
        CreateAgencyDto dto = buildCreateAgencyDto("Unauthorized Agency");

        mockMvc.perform(post(AGENCIES_BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenDuplicateAgencyName_whenCreateAgency_thenReturn400() throws Exception {
        String token = registerAndGetAccessToken("dupagency", "+4444444444");

        CreateAgencyDto dto = buildCreateAgencyDto("Duplicate Agency");
        mockMvc.perform(post(AGENCIES_BASE_URL + "/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Register a second user to attempt creating agency with same name
        String token2 = registerAndGetAccessToken("dupagency2", "+4444444445");

        mockMvc.perform(post(AGENCIES_BASE_URL + "/create")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenMissingRequiredFields_whenCreateAgency_thenReturn400() throws Exception {
        String token = registerAndGetAccessToken("badagency", "+5555555555");

        // Missing agencyName, contactNumber, city, address
        CreateAgencyDto dto = CreateAgencyDto.builder()
                .description("Missing required fields")
                .build();

        mockMvc.perform(post(AGENCIES_BASE_URL + "/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenAgencyExists_whenGetAgencyById_thenReturn200() throws Exception {
        String token = registerAndGetAccessToken("findagency", "+6666666666");

        CreateAgencyDto dto = buildCreateAgencyDto("Findable Agency");
        mockMvc.perform(post(AGENCIES_BASE_URL + "/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Get agency ID from /me
        MvcResult meResult = mockMvc.perform(get(AGENCIES_BASE_URL + "/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String body = meResult.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        String agencyId = node.get("agencyId").asText();

        // Get agency by ID
        mockMvc.perform(get(AGENCIES_BASE_URL + "/" + agencyId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agencyId").value(agencyId))
                .andExpect(jsonPath("$.agencyName").value("Findable Agency"));
    }

    @Test
    void givenUserWithNoAgency_whenGetMyAgencyProfile_thenReturnError() throws Exception {
        String token = registerAndGetAccessToken("noagency", "+7777777777");

        // User has no agency, should get 403 (AuthorizationException)
        mockMvc.perform(get(AGENCIES_BASE_URL + "/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
