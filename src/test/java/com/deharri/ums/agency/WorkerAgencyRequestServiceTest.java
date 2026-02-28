package com.deharri.ums.agency;

import com.deharri.ums.agency.dto.request.RespondToRequestDto;
import com.deharri.ums.agency.dto.request.SendInvitationDto;
import com.deharri.ums.agency.dto.response.WorkerAgencyRequestDto;
import com.deharri.ums.agency.entity.Agency;
import com.deharri.ums.agency.entity.AgencyMember;
import com.deharri.ums.agency.entity.WorkerAgencyRequest;
import com.deharri.ums.enums.AgencyRole;
import com.deharri.ums.enums.RequestStatus;
import com.deharri.ums.enums.RequestType;
import com.deharri.ums.enums.UserRole;
import com.deharri.ums.error.exception.AuthorizationException;
import com.deharri.ums.error.exception.CustomDataIntegrityViolationException;
import com.deharri.ums.error.exception.ResourceNotFoundException;
import com.deharri.ums.permission.PermissionService;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.user.entity.CoreUser;
import com.deharri.ums.user.entity.UserData;
import com.deharri.ums.worker.WorkerRepository;
import com.deharri.ums.worker.dto.request.JoinAgencyRequestDto;
import com.deharri.ums.worker.entity.Worker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkerAgencyRequestService Unit Tests")
class WorkerAgencyRequestServiceTest {

    @Mock
    private WorkerAgencyRequestRepository requestRepository;

    @Mock
    private AgencyRepository agencyRepository;

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private AgencyMemberRepository agencyMemberRepository;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private WorkerAgencyRequestService requestService;

    private CoreUser workerCoreUser;
    private CoreUser agencyCoreUser;
    private Worker worker;
    private Agency agency;
    private UUID workerId;
    private UUID agencyId;
    private UUID requestId;

    @BeforeEach
    void setUp() {
        workerId = UUID.randomUUID();
        agencyId = UUID.randomUUID();
        requestId = UUID.randomUUID();

        workerCoreUser = CoreUser.builder()
                .userId(UUID.randomUUID())
                .username("worker_user")
                .password("encodedPassword")
                .firstName("Ali")
                .lastName("Khan")
                .userData(UserData.builder()
                        .dataId(UUID.randomUUID())
                        .phoneNumber("+923001111111")
                        .email("ali@example.com")
                        .userRoles(new ArrayList<>(List.of(UserRole.ROLE_WORKER)))
                        .build())
                .build();

        agencyCoreUser = CoreUser.builder()
                .userId(UUID.randomUUID())
                .username("agency_admin")
                .password("encodedPassword")
                .firstName("Sara")
                .lastName("Ahmed")
                .userData(UserData.builder()
                        .dataId(UUID.randomUUID())
                        .phoneNumber("+923002222222")
                        .email("sara@example.com")
                        .userRoles(new ArrayList<>(List.of(UserRole.ROLE_AGENCY)))
                        .build())
                .build();

        worker = Worker.builder()
                .workerId(workerId)
                .coreUser(workerCoreUser)
                .workerType(Worker.WorkerType.ELECTRICIAN)
                .agency(null) // not part of any agency
                .totalJobsCompleted(0)
                .build();

        agency = Agency.builder()
                .agencyId(agencyId)
                .coreUser(agencyCoreUser)
                .agencyName("Test Agency")
                .totalWorkers(5)
                .build();
    }

    // ========================================================================
    // requestToJoinAgency() tests
    // ========================================================================

    @Nested
    @DisplayName("requestToJoinAgency()")
    class RequestToJoinAgencyTests {

        @Test
        @DisplayName("Should save join request when all validations pass")
        void givenValidRequest_whenRequestToJoinAgency_thenSaveRequest() {
            // given
            JoinAgencyRequestDto joinDto = JoinAgencyRequestDto.builder()
                    .agencyId(agencyId.toString())
                    .message("I would like to join your agency")
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(workerCoreUser);
            when(workerRepository.findByCoreUser(workerCoreUser)).thenReturn(Optional.of(worker));
            when(agencyRepository.findById(agencyId)).thenReturn(Optional.of(agency));
            when(requestRepository.existsByWorkerAndAgencyAndStatusAndRequestType(
                    worker, agency, RequestStatus.PENDING, RequestType.WORKER_REQUEST)).thenReturn(false);

            // when
            ResponseMessageDto result = requestService.requestToJoinAgency(joinDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Join request sent to Test Agency");

            ArgumentCaptor<WorkerAgencyRequest> requestCaptor = ArgumentCaptor.forClass(WorkerAgencyRequest.class);
            verify(requestRepository).save(requestCaptor.capture());

            WorkerAgencyRequest savedRequest = requestCaptor.getValue();
            assertThat(savedRequest.getWorker()).isEqualTo(worker);
            assertThat(savedRequest.getAgency()).isEqualTo(agency);
            assertThat(savedRequest.getRequestType()).isEqualTo(RequestType.WORKER_REQUEST);
            assertThat(savedRequest.getStatus()).isEqualTo(RequestStatus.PENDING);
            assertThat(savedRequest.getProposedRole()).isEqualTo(AgencyRole.AGENCY_WORKER);
            assertThat(savedRequest.getMessage()).isEqualTo("I would like to join your agency");
        }

        @Test
        @DisplayName("Should throw when worker already belongs to an agency")
        void givenWorkerAlreadyInAgency_whenRequestToJoinAgency_thenThrow() {
            // given
            worker.setAgency(agency); // worker already in an agency

            JoinAgencyRequestDto joinDto = JoinAgencyRequestDto.builder()
                    .agencyId(agencyId.toString())
                    .message("Please accept me")
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(workerCoreUser);
            when(workerRepository.findByCoreUser(workerCoreUser)).thenReturn(Optional.of(worker));
            when(agencyRepository.findById(agencyId)).thenReturn(Optional.of(agency));

            // when / then
            assertThatThrownBy(() -> requestService.requestToJoinAgency(joinDto))
                    .isInstanceOf(CustomDataIntegrityViolationException.class)
                    .hasMessage("You are already part of an agency. Leave your current agency first.");

            verify(requestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when a pending request already exists")
        void givenPendingRequestExists_whenRequestToJoinAgency_thenThrow() {
            // given
            JoinAgencyRequestDto joinDto = JoinAgencyRequestDto.builder()
                    .agencyId(agencyId.toString())
                    .message("Another request")
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(workerCoreUser);
            when(workerRepository.findByCoreUser(workerCoreUser)).thenReturn(Optional.of(worker));
            when(agencyRepository.findById(agencyId)).thenReturn(Optional.of(agency));
            when(requestRepository.existsByWorkerAndAgencyAndStatusAndRequestType(
                    worker, agency, RequestStatus.PENDING, RequestType.WORKER_REQUEST)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> requestService.requestToJoinAgency(joinDto))
                    .isInstanceOf(CustomDataIntegrityViolationException.class)
                    .hasMessage("You have already sent a request to this agency");

            verify(requestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when worker account not found")
        void givenNoWorkerAccount_whenRequestToJoinAgency_thenThrow() {
            // given
            JoinAgencyRequestDto joinDto = JoinAgencyRequestDto.builder()
                    .agencyId(agencyId.toString())
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(workerCoreUser);
            when(workerRepository.findByCoreUser(workerCoreUser)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> requestService.requestToJoinAgency(joinDto))
                    .isInstanceOf(AuthorizationException.class)
                    .hasMessage("Worker account not found");

            verify(requestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when agency ID is invalid")
        void givenInvalidAgencyId_whenRequestToJoinAgency_thenThrow() {
            // given
            UUID nonExistentAgencyId = UUID.randomUUID();
            JoinAgencyRequestDto joinDto = JoinAgencyRequestDto.builder()
                    .agencyId(nonExistentAgencyId.toString())
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(workerCoreUser);
            when(workerRepository.findByCoreUser(workerCoreUser)).thenReturn(Optional.of(worker));
            when(agencyRepository.findById(nonExistentAgencyId)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> requestService.requestToJoinAgency(joinDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Agency not found with ID:");

            verify(requestRepository, never()).save(any());
        }
    }

    // ========================================================================
    // sendInvitationToWorker() tests
    // ========================================================================

    @Nested
    @DisplayName("sendInvitationToWorker()")
    class SendInvitationToWorkerTests {

        @Test
        @DisplayName("Should save invitation when all validations pass")
        void givenValidInvitation_whenSendInvitationToWorker_thenSave() {
            // given
            SendInvitationDto invitationDto = SendInvitationDto.builder()
                    .workerId(workerId.toString())
                    .proposedRole(AgencyRole.AGENCY_WORKER)
                    .message("We'd like you to join us")
                    .build();

            AgencyMember adminMember = AgencyMember.builder()
                    .memberId(UUID.randomUUID())
                    .agency(agency)
                    .coreUser(agencyCoreUser)
                    .agencyRole(AgencyRole.AGENCY_ADMIN)
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(agencyCoreUser);
            when(agencyRepository.findByCoreUser(agencyCoreUser)).thenReturn(Optional.of(agency));
            when(agencyMemberRepository.findByAgencyAndCoreUser(agency, agencyCoreUser))
                    .thenReturn(Optional.of(adminMember));
            when(workerRepository.findById(workerId)).thenReturn(Optional.of(worker));
            when(requestRepository.existsByWorkerAndAgencyAndStatusAndRequestType(
                    worker, agency, RequestStatus.PENDING, RequestType.AGENCY_INVITATION)).thenReturn(false);

            // when
            ResponseMessageDto result = requestService.sendInvitationToWorker(invitationDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Invitation sent to worker");

            ArgumentCaptor<WorkerAgencyRequest> requestCaptor = ArgumentCaptor.forClass(WorkerAgencyRequest.class);
            verify(requestRepository).save(requestCaptor.capture());

            WorkerAgencyRequest savedRequest = requestCaptor.getValue();
            assertThat(savedRequest.getWorker()).isEqualTo(worker);
            assertThat(savedRequest.getAgency()).isEqualTo(agency);
            assertThat(savedRequest.getRequestType()).isEqualTo(RequestType.AGENCY_INVITATION);
            assertThat(savedRequest.getStatus()).isEqualTo(RequestStatus.PENDING);
            assertThat(savedRequest.getProposedRole()).isEqualTo(AgencyRole.AGENCY_WORKER);
            assertThat(savedRequest.getMessage()).isEqualTo("We'd like you to join us");
        }

        @Test
        @DisplayName("Should throw when member is not ADMIN or MANAGER")
        void givenNonAdminMember_whenSendInvitationToWorker_thenThrow() {
            // given
            SendInvitationDto invitationDto = SendInvitationDto.builder()
                    .workerId(workerId.toString())
                    .proposedRole(AgencyRole.AGENCY_WORKER)
                    .build();

            AgencyMember workerMember = AgencyMember.builder()
                    .memberId(UUID.randomUUID())
                    .agency(agency)
                    .coreUser(agencyCoreUser)
                    .agencyRole(AgencyRole.AGENCY_WORKER) // not admin or manager
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(agencyCoreUser);
            when(agencyRepository.findByCoreUser(agencyCoreUser)).thenReturn(Optional.of(agency));
            when(agencyMemberRepository.findByAgencyAndCoreUser(agency, agencyCoreUser))
                    .thenReturn(Optional.of(workerMember));

            // when / then
            assertThatThrownBy(() -> requestService.sendInvitationToWorker(invitationDto))
                    .isInstanceOf(AuthorizationException.class)
                    .hasMessage("Only agency admins and managers can send invitations");

            verify(requestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when worker already belongs to an agency")
        void givenWorkerAlreadyInAgency_whenSendInvitationToWorker_thenThrow() {
            // given
            worker.setAgency(agency); // worker already in agency

            SendInvitationDto invitationDto = SendInvitationDto.builder()
                    .workerId(workerId.toString())
                    .proposedRole(AgencyRole.AGENCY_WORKER)
                    .build();

            AgencyMember adminMember = AgencyMember.builder()
                    .memberId(UUID.randomUUID())
                    .agency(agency)
                    .coreUser(agencyCoreUser)
                    .agencyRole(AgencyRole.AGENCY_ADMIN)
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(agencyCoreUser);
            when(agencyRepository.findByCoreUser(agencyCoreUser)).thenReturn(Optional.of(agency));
            when(agencyMemberRepository.findByAgencyAndCoreUser(agency, agencyCoreUser))
                    .thenReturn(Optional.of(adminMember));
            when(workerRepository.findById(workerId)).thenReturn(Optional.of(worker));

            // when / then
            assertThatThrownBy(() -> requestService.sendInvitationToWorker(invitationDto))
                    .isInstanceOf(CustomDataIntegrityViolationException.class)
                    .hasMessage("Worker is already part of another agency");

            verify(requestRepository, never()).save(any());
        }
    }

    // ========================================================================
    // respondToInvitation() tests
    // ========================================================================

    @Nested
    @DisplayName("respondToInvitation()")
    class RespondToInvitationTests {

        @Test
        @DisplayName("Should accept invitation and link worker to agency")
        void givenAcceptResponse_whenRespondToInvitation_thenAcceptAndLinkWorker() {
            // given
            RespondToRequestDto respondDto = RespondToRequestDto.builder()
                    .accept(true)
                    .responseMessage("Happy to join!")
                    .build();

            WorkerAgencyRequest invitation = WorkerAgencyRequest.builder()
                    .requestId(requestId)
                    .worker(worker)
                    .agency(agency)
                    .requestType(RequestType.AGENCY_INVITATION)
                    .status(RequestStatus.PENDING)
                    .proposedRole(AgencyRole.AGENCY_WORKER)
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(workerCoreUser);
            when(workerRepository.findByCoreUser(workerCoreUser)).thenReturn(Optional.of(worker));
            when(requestRepository.findById(requestId)).thenReturn(Optional.of(invitation));

            // when
            ResponseMessageDto result = requestService.respondToInvitation(requestId.toString(), respondDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("You have joined Test Agency");

            assertThat(invitation.getStatus()).isEqualTo(RequestStatus.ACCEPTED);
            assertThat(invitation.getRespondedAt()).isNotNull();
            assertThat(invitation.getResponseMessage()).isEqualTo("Happy to join!");

            verify(requestRepository).save(invitation);
            verify(workerRepository).save(worker);
            assertThat(worker.getAgency()).isEqualTo(agency);

            ArgumentCaptor<AgencyMember> memberCaptor = ArgumentCaptor.forClass(AgencyMember.class);
            verify(agencyMemberRepository).save(memberCaptor.capture());
            AgencyMember savedMember = memberCaptor.getValue();
            assertThat(savedMember.getAgency()).isEqualTo(agency);
            assertThat(savedMember.getCoreUser()).isEqualTo(workerCoreUser);
            assertThat(savedMember.getAgencyRole()).isEqualTo(AgencyRole.AGENCY_WORKER);

            assertThat(agency.getTotalWorkers()).isEqualTo(6);
            verify(agencyRepository).save(agency);
        }

        @Test
        @DisplayName("Should reject invitation and update status")
        void givenRejectResponse_whenRespondToInvitation_thenRejectRequest() {
            // given
            RespondToRequestDto respondDto = RespondToRequestDto.builder()
                    .accept(false)
                    .responseMessage("Not interested at this time")
                    .build();

            WorkerAgencyRequest invitation = WorkerAgencyRequest.builder()
                    .requestId(requestId)
                    .worker(worker)
                    .agency(agency)
                    .requestType(RequestType.AGENCY_INVITATION)
                    .status(RequestStatus.PENDING)
                    .proposedRole(AgencyRole.AGENCY_WORKER)
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(workerCoreUser);
            when(workerRepository.findByCoreUser(workerCoreUser)).thenReturn(Optional.of(worker));
            when(requestRepository.findById(requestId)).thenReturn(Optional.of(invitation));

            // when
            ResponseMessageDto result = requestService.respondToInvitation(requestId.toString(), respondDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Invitation rejected");

            assertThat(invitation.getStatus()).isEqualTo(RequestStatus.REJECTED);
            assertThat(invitation.getRespondedAt()).isNotNull();
            assertThat(invitation.getResponseMessage()).isEqualTo("Not interested at this time");

            verify(requestRepository).save(invitation);
            verify(workerRepository, never()).save(any());
            verify(agencyMemberRepository, never()).save(any());
            verify(agencyRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when invitation is not for the current worker")
        void givenWrongWorker_whenRespondToInvitation_thenThrow() {
            // given
            Worker otherWorker = Worker.builder()
                    .workerId(UUID.randomUUID())
                    .coreUser(CoreUser.builder().userId(UUID.randomUUID()).build())
                    .build();

            WorkerAgencyRequest invitation = WorkerAgencyRequest.builder()
                    .requestId(requestId)
                    .worker(otherWorker) // invitation is for a different worker
                    .agency(agency)
                    .requestType(RequestType.AGENCY_INVITATION)
                    .status(RequestStatus.PENDING)
                    .build();

            RespondToRequestDto respondDto = RespondToRequestDto.builder()
                    .accept(true)
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(workerCoreUser);
            when(workerRepository.findByCoreUser(workerCoreUser)).thenReturn(Optional.of(worker));
            when(requestRepository.findById(requestId)).thenReturn(Optional.of(invitation));

            // when / then
            assertThatThrownBy(() -> requestService.respondToInvitation(requestId.toString(), respondDto))
                    .isInstanceOf(AuthorizationException.class)
                    .hasMessage("This invitation is not for you");

            verify(requestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when invitation has already been responded to")
        void givenAlreadyResponded_whenRespondToInvitation_thenThrow() {
            // given
            WorkerAgencyRequest invitation = WorkerAgencyRequest.builder()
                    .requestId(requestId)
                    .worker(worker)
                    .agency(agency)
                    .requestType(RequestType.AGENCY_INVITATION)
                    .status(RequestStatus.ACCEPTED) // already responded
                    .build();

            RespondToRequestDto respondDto = RespondToRequestDto.builder()
                    .accept(true)
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(workerCoreUser);
            when(workerRepository.findByCoreUser(workerCoreUser)).thenReturn(Optional.of(worker));
            when(requestRepository.findById(requestId)).thenReturn(Optional.of(invitation));

            // when / then
            assertThatThrownBy(() -> requestService.respondToInvitation(requestId.toString(), respondDto))
                    .isInstanceOf(CustomDataIntegrityViolationException.class)
                    .hasMessage("This invitation has already been responded to");

            verify(requestRepository, never()).save(any());
        }
    }

    // ========================================================================
    // respondToWorkerRequest() tests
    // ========================================================================

    @Nested
    @DisplayName("respondToWorkerRequest()")
    class RespondToWorkerRequestTests {

        @Test
        @DisplayName("Should accept worker request and link worker to agency")
        void givenAcceptResponse_whenRespondToWorkerRequest_thenAcceptAndLinkWorker() {
            // given
            RespondToRequestDto respondDto = RespondToRequestDto.builder()
                    .accept(true)
                    .responseMessage("Welcome aboard!")
                    .build();

            WorkerAgencyRequest workerRequest = WorkerAgencyRequest.builder()
                    .requestId(requestId)
                    .worker(worker)
                    .agency(agency)
                    .requestType(RequestType.WORKER_REQUEST)
                    .status(RequestStatus.PENDING)
                    .proposedRole(AgencyRole.AGENCY_WORKER)
                    .build();

            AgencyMember adminMember = AgencyMember.builder()
                    .memberId(UUID.randomUUID())
                    .agency(agency)
                    .coreUser(agencyCoreUser)
                    .agencyRole(AgencyRole.AGENCY_ADMIN)
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(agencyCoreUser);
            when(agencyRepository.findByCoreUser(agencyCoreUser)).thenReturn(Optional.of(agency));
            when(agencyMemberRepository.findByAgencyAndCoreUser(agency, agencyCoreUser))
                    .thenReturn(Optional.of(adminMember));
            when(requestRepository.findById(requestId)).thenReturn(Optional.of(workerRequest));

            // when
            ResponseMessageDto result = requestService.respondToWorkerRequest(requestId.toString(), respondDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Worker has been added to your agency");

            assertThat(workerRequest.getStatus()).isEqualTo(RequestStatus.ACCEPTED);
            assertThat(workerRequest.getRespondedAt()).isNotNull();
            assertThat(workerRequest.getResponseMessage()).isEqualTo("Welcome aboard!");

            verify(requestRepository).save(workerRequest);
            verify(workerRepository).save(worker);
            assertThat(worker.getAgency()).isEqualTo(agency);

            ArgumentCaptor<AgencyMember> memberCaptor = ArgumentCaptor.forClass(AgencyMember.class);
            // Two saves: one for the adminMember lookup, one for new member
            verify(agencyMemberRepository).save(memberCaptor.capture());
            AgencyMember savedMember = memberCaptor.getValue();
            assertThat(savedMember.getAgency()).isEqualTo(agency);
            assertThat(savedMember.getCoreUser()).isEqualTo(workerCoreUser);
            assertThat(savedMember.getAgencyRole()).isEqualTo(AgencyRole.AGENCY_WORKER);

            assertThat(agency.getTotalWorkers()).isEqualTo(6);
            verify(agencyRepository).save(agency);
        }

        @Test
        @DisplayName("Should reject worker request and update status")
        void givenRejectResponse_whenRespondToWorkerRequest_thenRejectRequest() {
            // given
            RespondToRequestDto respondDto = RespondToRequestDto.builder()
                    .accept(false)
                    .responseMessage("Not a good fit at the moment")
                    .build();

            WorkerAgencyRequest workerRequest = WorkerAgencyRequest.builder()
                    .requestId(requestId)
                    .worker(worker)
                    .agency(agency)
                    .requestType(RequestType.WORKER_REQUEST)
                    .status(RequestStatus.PENDING)
                    .proposedRole(AgencyRole.AGENCY_WORKER)
                    .build();

            AgencyMember adminMember = AgencyMember.builder()
                    .memberId(UUID.randomUUID())
                    .agency(agency)
                    .coreUser(agencyCoreUser)
                    .agencyRole(AgencyRole.AGENCY_ADMIN)
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(agencyCoreUser);
            when(agencyRepository.findByCoreUser(agencyCoreUser)).thenReturn(Optional.of(agency));
            when(agencyMemberRepository.findByAgencyAndCoreUser(agency, agencyCoreUser))
                    .thenReturn(Optional.of(adminMember));
            when(requestRepository.findById(requestId)).thenReturn(Optional.of(workerRequest));

            // when
            ResponseMessageDto result = requestService.respondToWorkerRequest(requestId.toString(), respondDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Worker request rejected");

            assertThat(workerRequest.getStatus()).isEqualTo(RequestStatus.REJECTED);
            assertThat(workerRequest.getRespondedAt()).isNotNull();
            assertThat(workerRequest.getResponseMessage()).isEqualTo("Not a good fit at the moment");

            verify(requestRepository).save(workerRequest);
            verify(workerRepository, never()).save(any());
            verify(agencyMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when member is not ADMIN or MANAGER")
        void givenNonAdminMember_whenRespondToWorkerRequest_thenThrow() {
            // given
            RespondToRequestDto respondDto = RespondToRequestDto.builder()
                    .accept(true)
                    .build();

            AgencyMember workerMember = AgencyMember.builder()
                    .memberId(UUID.randomUUID())
                    .agency(agency)
                    .coreUser(agencyCoreUser)
                    .agencyRole(AgencyRole.AGENCY_WORKER) // not admin or manager
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(agencyCoreUser);
            when(agencyRepository.findByCoreUser(agencyCoreUser)).thenReturn(Optional.of(agency));
            when(agencyMemberRepository.findByAgencyAndCoreUser(agency, agencyCoreUser))
                    .thenReturn(Optional.of(workerMember));

            // when / then
            assertThatThrownBy(() -> requestService.respondToWorkerRequest(requestId.toString(), respondDto))
                    .isInstanceOf(AuthorizationException.class)
                    .hasMessage("Only agency admins and managers can respond to worker requests");

            verify(requestRepository, never()).save(any());
            verify(workerRepository, never()).save(any());
        }
    }
}
