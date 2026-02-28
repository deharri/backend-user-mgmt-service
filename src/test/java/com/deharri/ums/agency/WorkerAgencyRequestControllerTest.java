package com.deharri.ums.agency;

import com.deharri.ums.agency.controller.WorkerAgencyRequestController;
import com.deharri.ums.agency.dto.request.RespondToRequestDto;
import com.deharri.ums.agency.dto.request.SendInvitationDto;
import com.deharri.ums.agency.dto.response.WorkerAgencyRequestDto;
import com.deharri.ums.enums.AgencyRole;
import com.deharri.ums.enums.RequestStatus;
import com.deharri.ums.enums.RequestType;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.worker.dto.request.JoinAgencyRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkerAgencyRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("WorkerAgencyRequestController Unit Tests")
class WorkerAgencyRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkerAgencyRequestService requestService;

    // ========================================================================
    // POST /api/v1/worker-agency-requests/worker/join-request
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/worker-agency-requests/worker/join-request")
    class RequestToJoinAgencyEndpointTests {

        @Test
        @DisplayName("Should return 201 when join request DTO is valid")
        void givenValidDto_whenRequestToJoinAgency_thenReturn201() throws Exception {
            // given
            JoinAgencyRequestDto joinDto = JoinAgencyRequestDto.builder()
                    .agencyId(UUID.randomUUID().toString())
                    .message("I want to join")
                    .build();

            ResponseMessageDto responseDto = new ResponseMessageDto("Join request sent to Test Agency");

            when(requestService.requestToJoinAgency(any(JoinAgencyRequestDto.class))).thenReturn(responseDto);

            // when / then
            mockMvc.perform(post("/api/v1/worker-agency-requests/worker/join-request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(joinDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("Join request sent to Test Agency"));
        }
    }

    // ========================================================================
    // GET /api/v1/worker-agency-requests/worker/my-requests
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/worker-agency-requests/worker/my-requests")
    class GetMyJoinRequestsEndpointTests {

        @Test
        @DisplayName("Should return 200 with list of join requests")
        void whenGetMyJoinRequests_thenReturn200WithList() throws Exception {
            // given
            WorkerAgencyRequestDto requestDto = WorkerAgencyRequestDto.builder()
                    .requestId(UUID.randomUUID().toString())
                    .workerId(UUID.randomUUID().toString())
                    .workerName("Ali Khan")
                    .agencyId(UUID.randomUUID().toString())
                    .agencyName("Test Agency")
                    .requestType(RequestType.WORKER_REQUEST)
                    .status(RequestStatus.PENDING)
                    .proposedRole(AgencyRole.AGENCY_WORKER)
                    .message("I want to join")
                    .createdAt(LocalDateTime.now())
                    .build();

            when(requestService.getMyJoinRequests()).thenReturn(List.of(requestDto));

            // when / then
            mockMvc.perform(get("/api/v1/worker-agency-requests/worker/my-requests"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].agencyName").value("Test Agency"))
                    .andExpect(jsonPath("$[0].requestType").value("WORKER_REQUEST"));
        }
    }

    // ========================================================================
    // GET /api/v1/worker-agency-requests/worker/invitations
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/worker-agency-requests/worker/invitations")
    class GetMyPendingInvitationsEndpointTests {

        @Test
        @DisplayName("Should return 200 with list of pending invitations")
        void whenGetMyPendingInvitations_thenReturn200WithList() throws Exception {
            // given
            WorkerAgencyRequestDto invitationDto = WorkerAgencyRequestDto.builder()
                    .requestId(UUID.randomUUID().toString())
                    .workerId(UUID.randomUUID().toString())
                    .workerName("Ali Khan")
                    .agencyId(UUID.randomUUID().toString())
                    .agencyName("Best Agency")
                    .requestType(RequestType.AGENCY_INVITATION)
                    .status(RequestStatus.PENDING)
                    .proposedRole(AgencyRole.AGENCY_WORKER)
                    .message("Join our team!")
                    .createdAt(LocalDateTime.now())
                    .build();

            when(requestService.getMyPendingInvitations()).thenReturn(List.of(invitationDto));

            // when / then
            mockMvc.perform(get("/api/v1/worker-agency-requests/worker/invitations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].agencyName").value("Best Agency"))
                    .andExpect(jsonPath("$[0].requestType").value("AGENCY_INVITATION"));
        }
    }

    // ========================================================================
    // POST /api/v1/worker-agency-requests/worker/invitations/{requestId}/respond
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/worker-agency-requests/worker/invitations/{requestId}/respond")
    class RespondToInvitationEndpointTests {

        @Test
        @DisplayName("Should return 200 when responding to invitation")
        void givenValidResponse_whenRespondToInvitation_thenReturn200() throws Exception {
            // given
            String requestId = UUID.randomUUID().toString();
            RespondToRequestDto respondDto = RespondToRequestDto.builder()
                    .accept(true)
                    .responseMessage("Happy to join!")
                    .build();

            ResponseMessageDto responseDto = new ResponseMessageDto("You have joined Test Agency");

            when(requestService.respondToInvitation(eq(requestId), any(RespondToRequestDto.class)))
                    .thenReturn(responseDto);

            // when / then
            mockMvc.perform(post("/api/v1/worker-agency-requests/worker/invitations/{requestId}/respond", requestId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(respondDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("You have joined Test Agency"));
        }
    }

    // ========================================================================
    // POST /api/v1/worker-agency-requests/agency/send-invitation
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/worker-agency-requests/agency/send-invitation")
    class SendInvitationToWorkerEndpointTests {

        @Test
        @DisplayName("Should return 201 when invitation DTO is valid")
        void givenValidDto_whenSendInvitationToWorker_thenReturn201() throws Exception {
            // given
            SendInvitationDto invitationDto = SendInvitationDto.builder()
                    .workerId(UUID.randomUUID().toString())
                    .proposedRole(AgencyRole.AGENCY_WORKER)
                    .message("Join our team!")
                    .build();

            ResponseMessageDto responseDto = new ResponseMessageDto("Invitation sent to worker");

            when(requestService.sendInvitationToWorker(any(SendInvitationDto.class))).thenReturn(responseDto);

            // when / then
            mockMvc.perform(post("/api/v1/worker-agency-requests/agency/send-invitation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invitationDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("Invitation sent to worker"));
        }
    }

    // ========================================================================
    // GET /api/v1/worker-agency-requests/agency/pending-requests
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/worker-agency-requests/agency/pending-requests")
    class GetAgencyPendingRequestsEndpointTests {

        @Test
        @DisplayName("Should return 200 with list of pending worker requests")
        void whenGetAgencyPendingRequests_thenReturn200WithList() throws Exception {
            // given
            WorkerAgencyRequestDto requestDto = WorkerAgencyRequestDto.builder()
                    .requestId(UUID.randomUUID().toString())
                    .workerId(UUID.randomUUID().toString())
                    .workerName("Ali Khan")
                    .workerType("Electrician")
                    .agencyId(UUID.randomUUID().toString())
                    .agencyName("Test Agency")
                    .requestType(RequestType.WORKER_REQUEST)
                    .status(RequestStatus.PENDING)
                    .proposedRole(AgencyRole.AGENCY_WORKER)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(requestService.getAgencyPendingRequests()).thenReturn(List.of(requestDto));

            // when / then
            mockMvc.perform(get("/api/v1/worker-agency-requests/agency/pending-requests"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].workerName").value("Ali Khan"))
                    .andExpect(jsonPath("$[0].status").value("PENDING"));
        }
    }

    // ========================================================================
    // GET /api/v1/worker-agency-requests/agency/sent-invitations
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/worker-agency-requests/agency/sent-invitations")
    class GetAgencySentInvitationsEndpointTests {

        @Test
        @DisplayName("Should return 200 with list of sent invitations")
        void whenGetAgencySentInvitations_thenReturn200WithList() throws Exception {
            // given
            WorkerAgencyRequestDto invitationDto = WorkerAgencyRequestDto.builder()
                    .requestId(UUID.randomUUID().toString())
                    .workerId(UUID.randomUUID().toString())
                    .workerName("Hassan Raza")
                    .workerType("Plumber")
                    .agencyId(UUID.randomUUID().toString())
                    .agencyName("Test Agency")
                    .requestType(RequestType.AGENCY_INVITATION)
                    .status(RequestStatus.PENDING)
                    .proposedRole(AgencyRole.AGENCY_WORKER)
                    .message("We'd like you to join us")
                    .createdAt(LocalDateTime.now())
                    .build();

            when(requestService.getAgencySentInvitations()).thenReturn(List.of(invitationDto));

            // when / then
            mockMvc.perform(get("/api/v1/worker-agency-requests/agency/sent-invitations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].workerName").value("Hassan Raza"))
                    .andExpect(jsonPath("$[0].requestType").value("AGENCY_INVITATION"));
        }
    }

    // ========================================================================
    // POST /api/v1/worker-agency-requests/agency/requests/{requestId}/respond
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/worker-agency-requests/agency/requests/{requestId}/respond")
    class RespondToWorkerRequestEndpointTests {

        @Test
        @DisplayName("Should return 200 when responding to worker request")
        void givenValidResponse_whenRespondToWorkerRequest_thenReturn200() throws Exception {
            // given
            String requestId = UUID.randomUUID().toString();
            RespondToRequestDto respondDto = RespondToRequestDto.builder()
                    .accept(true)
                    .responseMessage("Welcome aboard!")
                    .build();

            ResponseMessageDto responseDto = new ResponseMessageDto("Worker has been added to your agency");

            when(requestService.respondToWorkerRequest(eq(requestId), any(RespondToRequestDto.class)))
                    .thenReturn(responseDto);

            // when / then
            mockMvc.perform(post("/api/v1/worker-agency-requests/agency/requests/{requestId}/respond", requestId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(respondDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Worker has been added to your agency"));
        }
    }
}
