package com.deharri.ums.worker;

import com.deharri.ums.enums.Language;
import com.deharri.ums.enums.PakistanCity;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.worker.dto.request.CreateWorkerAccountDto;
import com.deharri.ums.worker.dto.request.UpdateWorkerProfileDto;
import com.deharri.ums.worker.dto.response.WorkerListItemDto;
import com.deharri.ums.worker.dto.response.WorkerProfileResponseDto;
import com.deharri.ums.worker.entity.AvailabilityStatus;
import com.deharri.ums.worker.entity.Worker;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkerController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("WorkerController Unit Tests")
class WorkerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkerService workerService;

    // ========================================================================
    // POST /api/v1/workers/create
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/workers/create")
    class CreateWorkerAccountTests {

        @Test
        @DisplayName("Should return 201 when valid DTO is provided")
        void givenValidDto_whenCreateWorkerAccount_thenReturn201() throws Exception {
            // given
            CreateWorkerAccountDto dto = CreateWorkerAccountDto.builder()
                    .workerType(Worker.WorkerType.ELECTRICIAN)
                    .skills(List.of("Wiring", "Circuit Repair"))
                    .cnic("12345-1234567-1")
                    .bio("Experienced electrician")
                    .experienceYears(5)
                    .hourlyRate(new BigDecimal("500.00"))
                    .dailyRate(new BigDecimal("4000.00"))
                    .city(PakistanCity.LAHORE)
                    .area("Gulberg")
                    .serviceCities(List.of(PakistanCity.LAHORE))
                    .languages(List.of(Language.URDU))
                    .build();

            ResponseMessageDto response = new ResponseMessageDto("Worker account created successfully");
            when(workerService.createWorkerAccount(any(CreateWorkerAccountDto.class))).thenReturn(response);

            // when / then
            mockMvc.perform(post("/api/v1/workers/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("Worker account created successfully"));
        }
    }

    // ========================================================================
    // POST /api/v1/workers/verify/cnic
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/workers/verify/cnic")
    class SubmitCnicTests {

        @Test
        @DisplayName("Should return 200 when multipart CNIC files are submitted")
        void givenMultipartFiles_whenSubmitCnic_thenReturn200() throws Exception {
            // given
            MockMultipartFile cnicFront = new MockMultipartFile(
                    "cnicFront", "cnic_front.jpg", MediaType.IMAGE_JPEG_VALUE, "front-content".getBytes());
            MockMultipartFile cnicBack = new MockMultipartFile(
                    "cnicBack", "cnic_back.jpg", MediaType.IMAGE_JPEG_VALUE, "back-content".getBytes());

            when(workerService.submitCnicForVerification(any(), any()))
                    .thenReturn("CNIC submitted for verification successfully");

            // when / then
            mockMvc.perform(multipart("/api/v1/workers/verify/cnic")
                            .file(cnicFront)
                            .file(cnicBack))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("CNIC submitted for verification successfully"));
        }
    }

    // ========================================================================
    // GET /api/v1/workers
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/workers")
    class GetAllWorkersTests {

        @Test
        @DisplayName("Should return 200 with list of workers")
        void whenGetAllWorkers_thenReturn200WithList() throws Exception {
            // given
            WorkerListItemDto worker1 = WorkerListItemDto.builder()
                    .workerId(UUID.randomUUID().toString())
                    .firstName("John")
                    .lastName("Doe")
                    .workerType(Worker.WorkerType.ELECTRICIAN)
                    .experienceYears(5)
                    .hourlyRate(new BigDecimal("500.00"))
                    .city(PakistanCity.LAHORE)
                    .availabilityStatus(AvailabilityStatus.Status.AVAILABLE)
                    .averageRating(new BigDecimal("4.50"))
                    .totalJobsCompleted(25)
                    .build();

            WorkerListItemDto worker2 = WorkerListItemDto.builder()
                    .workerId(UUID.randomUUID().toString())
                    .firstName("Jane")
                    .lastName("Smith")
                    .workerType(Worker.WorkerType.PLUMBER)
                    .experienceYears(3)
                    .hourlyRate(new BigDecimal("400.00"))
                    .city(PakistanCity.KARACHI)
                    .availabilityStatus(AvailabilityStatus.Status.BUSY)
                    .build();

            when(workerService.getAllWorkers()).thenReturn(List.of(worker1, worker2));

            // when / then
            mockMvc.perform(get("/api/v1/workers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].firstName").value("John"))
                    .andExpect(jsonPath("$[1].firstName").value("Jane"));
        }
    }

    // ========================================================================
    // GET /api/v1/workers/{workerId}
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/workers/{workerId}")
    class GetWorkerByIdTests {

        @Test
        @DisplayName("Should return 200 with worker profile when valid ID is provided")
        void givenValidId_whenGetWorkerById_thenReturn200() throws Exception {
            // given
            String workerId = UUID.randomUUID().toString();

            WorkerProfileResponseDto profileDto = WorkerProfileResponseDto.builder()
                    .workerId(workerId)
                    .firstName("John")
                    .lastName("Doe")
                    .workerType(Worker.WorkerType.ELECTRICIAN)
                    .experienceYears(5)
                    .hourlyRate(new BigDecimal("500.00"))
                    .build();

            when(workerService.getWorkerById(workerId)).thenReturn(profileDto);

            // when / then
            mockMvc.perform(get("/api/v1/workers/{workerId}", workerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.workerId").value(workerId))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"));
        }
    }

    // ========================================================================
    // GET /api/v1/workers/me
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/workers/me")
    class GetMyWorkerProfileTests {

        @Test
        @DisplayName("Should return 200 with current user's worker profile")
        void whenGetMyWorkerProfile_thenReturn200() throws Exception {
            // given
            WorkerProfileResponseDto profileDto = WorkerProfileResponseDto.builder()
                    .workerId(UUID.randomUUID().toString())
                    .userId(UUID.randomUUID().toString())
                    .firstName("John")
                    .lastName("Doe")
                    .workerType(Worker.WorkerType.ELECTRICIAN)
                    .skills(List.of("Wiring", "Circuit Repair"))
                    .bio("Experienced electrician")
                    .build();

            when(workerService.getMyWorkerProfile()).thenReturn(profileDto);

            // when / then
            mockMvc.perform(get("/api/v1/workers/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.workerType").value("ELECTRICIAN"))
                    .andExpect(jsonPath("$.bio").value("Experienced electrician"));
        }
    }

    // ========================================================================
    // PUT /api/v1/workers/me
    // ========================================================================

    @Nested
    @DisplayName("PUT /api/v1/workers/me")
    class UpdateMyWorkerProfileTests {

        @Test
        @DisplayName("Should return 200 when valid update DTO is provided")
        void givenValidDto_whenUpdateMyWorkerProfile_thenReturn200() throws Exception {
            // given
            UpdateWorkerProfileDto updateDto = UpdateWorkerProfileDto.builder()
                    .skills(List.of("Wiring", "Solar Panel Installation"))
                    .bio("Updated bio")
                    .experienceYears(7)
                    .hourlyRate(new BigDecimal("600.00"))
                    .dailyRate(new BigDecimal("4500.00"))
                    .city(PakistanCity.ISLAMABAD)
                    .area("F-8")
                    .serviceCities(List.of(PakistanCity.ISLAMABAD, PakistanCity.RAWALPINDI))
                    .languages(List.of(Language.URDU, Language.ENGLISH))
                    .build();

            ResponseMessageDto response = new ResponseMessageDto("Worker profile updated successfully");
            when(workerService.updateMyWorkerProfile(any(UpdateWorkerProfileDto.class))).thenReturn(response);

            // when / then
            mockMvc.perform(put("/api/v1/workers/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Worker profile updated successfully"));
        }
    }

    // ========================================================================
    // POST /api/v1/workers/portfolio/upload
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/workers/portfolio/upload")
    class UploadPortfolioImageTests {

        @Test
        @DisplayName("Should return 201 when valid portfolio image is uploaded")
        void givenValidFile_whenUploadPortfolioImage_thenReturn201() throws Exception {
            // given
            MockMultipartFile portfolioImage = new MockMultipartFile(
                    "portfolioImage", "portfolio_1.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes());

            ResponseMessageDto response = new ResponseMessageDto("Portfolio image uploaded successfully");
            when(workerService.uploadPortfolioImage(any())).thenReturn(response);

            // when / then
            mockMvc.perform(multipart("/api/v1/workers/portfolio/upload")
                            .file(portfolioImage))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("Portfolio image uploaded successfully"));
        }
    }

    // ========================================================================
    // DELETE /api/v1/workers/portfolio
    // ========================================================================

    @Nested
    @DisplayName("DELETE /api/v1/workers/portfolio")
    class DeletePortfolioImageTests {

        @Test
        @DisplayName("Should return 200 when image path is provided")
        void givenImagePath_whenDeletePortfolioImage_thenReturn200() throws Exception {
            // given
            String imagePath = "deharri/users/some-uuid/2026/02/24/image.jpg";

            ResponseMessageDto response = new ResponseMessageDto("Portfolio image deleted successfully");
            when(workerService.deletePortfolioImage(imagePath)).thenReturn(response);

            // when / then
            mockMvc.perform(delete("/api/v1/workers/portfolio")
                            .param("imagePath", imagePath))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Portfolio image deleted successfully"));
        }
    }
}
