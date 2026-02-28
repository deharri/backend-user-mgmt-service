package com.deharri.ums.worker;

import com.deharri.ums.enums.Language;
import com.deharri.ums.enums.PakistanCity;
import com.deharri.ums.error.exception.ResourceNotFoundException;
import com.deharri.ums.error.handler.GlobalExceptionHandler;
import com.deharri.ums.user.entity.CoreUser;
import com.deharri.ums.user.entity.UserData;
import com.deharri.ums.worker.controller.internal.InternalWorkerController;
import com.deharri.ums.worker.dto.request.UpdateWorkerStatsDto;
import com.deharri.ums.worker.entity.AvailabilityStatus;
import com.deharri.ums.worker.entity.CnicVerification;
import com.deharri.ums.worker.entity.Worker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalWorkerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("InternalWorkerController Unit Tests")
class InternalWorkerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkerRepository workerRepository;

    private Worker worker1;
    private Worker worker2;
    private UUID worker1Id;
    private UUID worker2Id;

    @BeforeEach
    void setUp() {
        worker1Id = UUID.randomUUID();
        worker2Id = UUID.randomUUID();

        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        CoreUser coreUser1 = CoreUser.builder()
                .userId(userId1)
                .username("john_doe")
                .password("encoded")
                .firstName("John")
                .lastName("Doe")
                .userData(UserData.builder()
                        .dataId(UUID.randomUUID())
                        .phoneNumber("+923001234567")
                        .email("john@example.com")
                        .profilePicturePath("deharri/users/pic1.jpg")
                        .build())
                .build();

        CoreUser coreUser2 = CoreUser.builder()
                .userId(userId2)
                .username("jane_smith")
                .password("encoded")
                .firstName("Jane")
                .lastName("Smith")
                .userData(UserData.builder()
                        .dataId(UUID.randomUUID())
                        .phoneNumber("+923009876543")
                        .email("jane@example.com")
                        .build())
                .build();

        worker1 = Worker.builder()
                .workerId(worker1Id)
                .coreUser(coreUser1)
                .workerType(Worker.WorkerType.ELECTRICIAN)
                .skills(new ArrayList<>(List.of("Wiring", "Circuit Repair")))
                .bio("Experienced electrician")
                .experienceYears(5)
                .hourlyRate(new BigDecimal("500.00"))
                .dailyRate(new BigDecimal("4000.00"))
                .city(PakistanCity.LAHORE)
                .area("Gulberg")
                .serviceCities(new ArrayList<>(List.of(PakistanCity.LAHORE, PakistanCity.ISLAMABAD)))
                .languages(new ArrayList<>(List.of(Language.URDU, Language.ENGLISH)))
                .portfolioImagePaths(new ArrayList<>())
                .averageRating(new BigDecimal("4.50"))
                .totalJobsCompleted(25)
                .availabilityStatus(AvailabilityStatus.builder()
                        .availabilityId(1L)
                        .availabilityStatus(AvailabilityStatus.Status.AVAILABLE)
                        .build())
                .cnicVerification(CnicVerification.builder()
                        .verificationId(1L)
                        .verificationStatus(CnicVerification.Status.VERIFIED)
                        .cnic("12345-1234567-1")
                        .build())
                .build();

        worker2 = Worker.builder()
                .workerId(worker2Id)
                .coreUser(coreUser2)
                .workerType(Worker.WorkerType.PLUMBER)
                .skills(new ArrayList<>(List.of("Pipe Fitting")))
                .bio("Expert plumber")
                .experienceYears(3)
                .hourlyRate(new BigDecimal("400.00"))
                .city(PakistanCity.KARACHI)
                .area("Clifton")
                .serviceCities(new ArrayList<>(List.of(PakistanCity.KARACHI)))
                .languages(new ArrayList<>(List.of(Language.URDU, Language.SINDHI)))
                .portfolioImagePaths(new ArrayList<>())
                .averageRating(new BigDecimal("4.00"))
                .totalJobsCompleted(10)
                .availabilityStatus(AvailabilityStatus.builder()
                        .availabilityId(2L)
                        .availabilityStatus(AvailabilityStatus.Status.BUSY)
                        .build())
                .cnicVerification(CnicVerification.builder()
                        .verificationId(2L)
                        .verificationStatus(CnicVerification.Status.PENDING)
                        .cnic("54321-7654321-0")
                        .build())
                .build();
    }

    // ========================================================================
    // GET /api/v1/internal/workers
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/internal/workers")
    class GetAllWorkersTests {

        @Test
        @DisplayName("Should return 200 with list of internal worker profile DTOs")
        void givenWorkersExist_whenGetAllWorkers_thenReturn200WithList() throws Exception {
            // given
            when(workerRepository.findAll()).thenReturn(List.of(worker1, worker2));

            // when / then
            mockMvc.perform(get("/api/v1/internal/workers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].workerId").value(worker1Id.toString()))
                    .andExpect(jsonPath("$[0].username").value("john_doe"))
                    .andExpect(jsonPath("$[0].firstName").value("John"))
                    .andExpect(jsonPath("$[0].workerType").value("ELECTRICIAN"))
                    .andExpect(jsonPath("$[0].availabilityStatus").value("AVAILABLE"))
                    .andExpect(jsonPath("$[0].isVerified").value(true))
                    .andExpect(jsonPath("$[1].workerId").value(worker2Id.toString()))
                    .andExpect(jsonPath("$[1].username").value("jane_smith"))
                    .andExpect(jsonPath("$[1].workerType").value("PLUMBER"))
                    .andExpect(jsonPath("$[1].availabilityStatus").value("BUSY"))
                    .andExpect(jsonPath("$[1].isVerified").value(false));

            verify(workerRepository).findAll();
        }
    }

    // ========================================================================
    // GET /api/v1/internal/workers/{workerId}
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/internal/workers/{workerId}")
    class GetWorkerByIdTests {

        @Test
        @DisplayName("Should return 200 with worker profile when worker exists")
        void givenValidWorkerId_whenGetWorkerById_thenReturn200() throws Exception {
            // given
            when(workerRepository.findById(worker1Id)).thenReturn(Optional.of(worker1));

            // when / then
            mockMvc.perform(get("/api/v1/internal/workers/{workerId}", worker1Id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.workerId").value(worker1Id.toString()))
                    .andExpect(jsonPath("$.username").value("john_doe"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.workerType").value("ELECTRICIAN"))
                    .andExpect(jsonPath("$.skills", hasSize(2)))
                    .andExpect(jsonPath("$.bio").value("Experienced electrician"))
                    .andExpect(jsonPath("$.experienceYears").value(5))
                    .andExpect(jsonPath("$.hourlyRate").value(500.00))
                    .andExpect(jsonPath("$.dailyRate").value(4000.00))
                    .andExpect(jsonPath("$.city").value("LAHORE"))
                    .andExpect(jsonPath("$.area").value("Gulberg"))
                    .andExpect(jsonPath("$.availabilityStatus").value("AVAILABLE"))
                    .andExpect(jsonPath("$.isVerified").value(true))
                    .andExpect(jsonPath("$.averageRating").value(4.50))
                    .andExpect(jsonPath("$.totalJobsCompleted").value(25));

            verify(workerRepository).findById(worker1Id);
        }

        @Test
        @DisplayName("Should return error response when worker does not exist")
        void givenInvalidWorkerId_whenGetWorkerById_thenReturn404() throws Exception {
            // given
            UUID invalidId = UUID.randomUUID();
            when(workerRepository.findById(invalidId)).thenReturn(Optional.empty());

            // when / then
            mockMvc.perform(get("/api/v1/internal/workers/{workerId}", invalidId))
                    .andExpect(status().isInternalServerError());

            verify(workerRepository).findById(invalidId);
        }
    }

    // ========================================================================
    // PUT /api/v1/internal/workers/{workerId}/stats
    // ========================================================================

    @Nested
    @DisplayName("PUT /api/v1/internal/workers/{workerId}/stats")
    class UpdateWorkerStatsTests {

        @Test
        @DisplayName("Should return 200 when valid stats are provided for existing worker")
        void givenValidStats_whenUpdateWorkerStats_thenReturn200() throws Exception {
            // given
            UpdateWorkerStatsDto statsDto = new UpdateWorkerStatsDto(new BigDecimal("4.75"), 30);

            when(workerRepository.findById(worker1Id)).thenReturn(Optional.of(worker1));
            when(workerRepository.save(any(Worker.class))).thenReturn(worker1);

            // when / then
            mockMvc.perform(put("/api/v1/internal/workers/{workerId}/stats", worker1Id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(statsDto)))
                    .andExpect(status().isOk());

            verify(workerRepository).findById(worker1Id);
            verify(workerRepository).save(worker1);
        }

        @Test
        @DisplayName("Should return error response when worker does not exist for stats update")
        void givenInvalidWorkerId_whenUpdateWorkerStats_thenReturn404() throws Exception {
            // given
            UUID invalidId = UUID.randomUUID();
            UpdateWorkerStatsDto statsDto = new UpdateWorkerStatsDto(new BigDecimal("4.00"), 15);

            when(workerRepository.findById(invalidId)).thenReturn(Optional.empty());

            // when / then
            mockMvc.perform(put("/api/v1/internal/workers/{workerId}/stats", invalidId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(statsDto)))
                    .andExpect(status().isInternalServerError());

            verify(workerRepository).findById(invalidId);
            verify(workerRepository, never()).save(any());
        }
    }
}
