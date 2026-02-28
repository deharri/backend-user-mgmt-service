package com.deharri.ums.agency;

import com.deharri.ums.agency.controller.AgencyController;
import com.deharri.ums.agency.dto.request.CreateAgencyDto;
import com.deharri.ums.agency.dto.request.UpdateAgencyDto;
import com.deharri.ums.agency.dto.response.AgencyListItemDto;
import com.deharri.ums.agency.dto.response.AgencyProfileResponseDto;
import com.deharri.ums.agency.entity.Agency;
import com.deharri.ums.enums.PakistanCity;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgencyController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AgencyController Unit Tests")
class AgencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AgencyService agencyService;

    // ========================================================================
    // POST /api/v1/agencies/create
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/agencies/create")
    class CreateAgencyEndpointTests {

        @Test
        @DisplayName("Should return 201 when agency creation DTO is valid")
        void givenValidDto_whenCreateAgency_thenReturn201() throws Exception {
            // given
            CreateAgencyDto createDto = CreateAgencyDto.builder()
                    .agencyName("New Agency")
                    .description("A new agency")
                    .contactNumber("+923001234567")
                    .contactEmail("agency@example.com")
                    .city(PakistanCity.LAHORE)
                    .address("123 Agency Street")
                    .serviceCities(List.of(PakistanCity.LAHORE))
                    .licenseNumber("LIC-001")
                    .build();

            ResponseMessageDto responseDto = new ResponseMessageDto(
                    "Agency created successfully. You are now the agency admin.");

            when(agencyService.createAgency(any(CreateAgencyDto.class))).thenReturn(responseDto);

            // when / then
            mockMvc.perform(post("/api/v1/agencies/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value(
                            "Agency created successfully. You are now the agency admin."));
        }
    }

    // ========================================================================
    // GET /api/v1/agencies
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/agencies")
    class GetAllAgenciesEndpointTests {

        @Test
        @DisplayName("Should return 200 with list of agencies")
        void whenGetAllAgencies_thenReturn200WithList() throws Exception {
            // given
            AgencyListItemDto dto1 = AgencyListItemDto.builder()
                    .agencyId(UUID.randomUUID().toString())
                    .agencyName("Agency 1")
                    .city(PakistanCity.LAHORE)
                    .verificationStatus(Agency.VerificationStatus.VERIFIED)
                    .totalWorkers(5)
                    .averageRating(new BigDecimal("4.50"))
                    .totalJobsCompleted(10)
                    .build();

            AgencyListItemDto dto2 = AgencyListItemDto.builder()
                    .agencyId(UUID.randomUUID().toString())
                    .agencyName("Agency 2")
                    .city(PakistanCity.KARACHI)
                    .verificationStatus(Agency.VerificationStatus.PENDING)
                    .totalWorkers(3)
                    .averageRating(new BigDecimal("3.80"))
                    .totalJobsCompleted(7)
                    .build();

            when(agencyService.getAllAgencies()).thenReturn(List.of(dto1, dto2));

            // when / then
            mockMvc.perform(get("/api/v1/agencies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].agencyName").value("Agency 1"))
                    .andExpect(jsonPath("$[1].agencyName").value("Agency 2"));
        }
    }

    // ========================================================================
    // GET /api/v1/agencies/{agencyId}
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/agencies/{agencyId}")
    class GetAgencyByIdEndpointTests {

        @Test
        @DisplayName("Should return 200 with agency profile when ID is valid")
        void givenValidId_whenGetAgencyById_thenReturn200() throws Exception {
            // given
            String agencyId = UUID.randomUUID().toString();

            AgencyProfileResponseDto profileDto = AgencyProfileResponseDto.builder()
                    .agencyId(agencyId)
                    .agencyName("Test Agency")
                    .city(PakistanCity.LAHORE)
                    .verificationStatus(Agency.VerificationStatus.VERIFIED)
                    .totalWorkers(10)
                    .build();

            when(agencyService.getAgencyById(agencyId)).thenReturn(profileDto);

            // when / then
            mockMvc.perform(get("/api/v1/agencies/{agencyId}", agencyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.agencyId").value(agencyId))
                    .andExpect(jsonPath("$.agencyName").value("Test Agency"))
                    .andExpect(jsonPath("$.totalWorkers").value(10));
        }
    }

    // ========================================================================
    // GET /api/v1/agencies/me
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/agencies/me")
    class GetMyAgencyProfileEndpointTests {

        @Test
        @DisplayName("Should return 200 with current user's agency profile")
        void whenGetMyAgencyProfile_thenReturn200() throws Exception {
            // given
            AgencyProfileResponseDto profileDto = AgencyProfileResponseDto.builder()
                    .agencyId(UUID.randomUUID().toString())
                    .agencyName("My Agency")
                    .userId(UUID.randomUUID().toString())
                    .username("agency_owner")
                    .build();

            when(agencyService.getMyAgencyProfile()).thenReturn(profileDto);

            // when / then
            mockMvc.perform(get("/api/v1/agencies/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.agencyName").value("My Agency"))
                    .andExpect(jsonPath("$.username").value("agency_owner"));
        }
    }

    // ========================================================================
    // PUT /api/v1/agencies/me
    // ========================================================================

    @Nested
    @DisplayName("PUT /api/v1/agencies/me")
    class UpdateMyAgencyProfileEndpointTests {

        @Test
        @DisplayName("Should return 200 when update DTO is valid")
        void givenValidDto_whenUpdateMyAgencyProfile_thenReturn200() throws Exception {
            // given
            UpdateAgencyDto updateDto = UpdateAgencyDto.builder()
                    .agencyName("Updated Agency")
                    .description("Updated description")
                    .contactNumber("+923009876543")
                    .build();

            ResponseMessageDto responseDto = new ResponseMessageDto("Agency profile updated successfully");

            when(agencyService.updateMyAgencyProfile(any(UpdateAgencyDto.class))).thenReturn(responseDto);

            // when / then
            mockMvc.perform(put("/api/v1/agencies/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Agency profile updated successfully"));
        }
    }

    // ========================================================================
    // POST /api/v1/agencies/license/upload
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/agencies/license/upload")
    class UploadLicenseDocumentEndpointTests {

        @Test
        @DisplayName("Should return 201 when license document upload is valid")
        void givenValidFile_whenUploadLicenseDocument_thenReturn201() throws Exception {
            // given
            MockMultipartFile licenseFile = new MockMultipartFile(
                    "licenseDocument",
                    "license.pdf",
                    MediaType.APPLICATION_PDF_VALUE,
                    "license content".getBytes()
            );

            ResponseMessageDto responseDto = new ResponseMessageDto("License document uploaded successfully");

            when(agencyService.uploadLicenseDocument(any())).thenReturn(responseDto);

            // when / then
            mockMvc.perform(multipart("/api/v1/agencies/license/upload")
                            .file(licenseFile))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("License document uploaded successfully"));
        }
    }
}
