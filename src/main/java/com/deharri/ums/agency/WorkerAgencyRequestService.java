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
import com.deharri.ums.worker.WorkerRepository;
import com.deharri.ums.worker.dto.request.JoinAgencyRequestDto;
import com.deharri.ums.worker.entity.Worker;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class WorkerAgencyRequestService {

    private final WorkerAgencyRequestRepository requestRepository;
    private final AgencyRepository agencyRepository;
    private final WorkerRepository workerRepository;
    private final AgencyMemberRepository agencyMemberRepository;
    private final PermissionService permissionService;

    // WORKER-INITIATED: Request to join an agency
    @Transactional
    public ResponseMessageDto requestToJoinAgency(JoinAgencyRequestDto joinRequestDto) {
        var currentUser = permissionService.getLoggedInUser();
        Worker worker = workerRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Worker account not found"));

        UUID agencyId = UUID.fromString(joinRequestDto.getAgencyId());
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found with ID: " + joinRequestDto.getAgencyId()));

        // Check if worker already belongs to an agency
        if (worker.getAgency() != null) {
            throw new CustomDataIntegrityViolationException("You are already part of an agency. Leave your current agency first.");
        }

        // Check if there's already a pending request
        if (requestRepository.existsByWorkerAndAgencyAndStatusAndRequestType(
                worker, agency, RequestStatus.PENDING, RequestType.WORKER_REQUEST)) {
            throw new CustomDataIntegrityViolationException("You have already sent a request to this agency");
        }

        // Create worker request
        WorkerAgencyRequest request = WorkerAgencyRequest.builder()
                .worker(worker)
                .agency(agency)
                .requestType(RequestType.WORKER_REQUEST)
                .status(RequestStatus.PENDING)
                .proposedRole(AgencyRole.AGENCY_WORKER) // Workers request as AGENCY_WORKER by default
                .message(joinRequestDto.getMessage())
                .build();

        requestRepository.save(request);

        return new ResponseMessageDto("Join request sent to " + agency.getAgencyName());
    }

    // AGENCY-INITIATED: Send invitation to a worker
    @Transactional
    public ResponseMessageDto sendInvitationToWorker(SendInvitationDto invitationDto) {
        var currentUser = permissionService.getLoggedInUser();
        Agency agency = agencyRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Agency account not found"));

        // Check if user has permission (ADMIN or MANAGER)
        AgencyMember member = agencyMemberRepository.findByAgencyAndCoreUser(agency, currentUser)
                .orElseThrow(() -> new AuthorizationException("You are not a member of this agency"));

        if (member.getAgencyRole() != AgencyRole.AGENCY_ADMIN &&
                member.getAgencyRole() != AgencyRole.AGENCY_MANAGER) {
            throw new AuthorizationException("Only agency admins and managers can send invitations");
        }

        UUID workerId = UUID.fromString(invitationDto.getWorkerId());
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found with ID: " + invitationDto.getWorkerId()));

        // Check if worker already belongs to an agency
        if (worker.getAgency() != null) {
            throw new CustomDataIntegrityViolationException("Worker is already part of another agency");
        }

        // Check if there's already a pending invitation
        if (requestRepository.existsByWorkerAndAgencyAndStatusAndRequestType(
                worker, agency, RequestStatus.PENDING, RequestType.AGENCY_INVITATION)) {
            throw new CustomDataIntegrityViolationException("An invitation has already been sent to this worker");
        }

        // Create agency invitation
        WorkerAgencyRequest request = WorkerAgencyRequest.builder()
                .worker(worker)
                .agency(agency)
                .requestType(RequestType.AGENCY_INVITATION)
                .status(RequestStatus.PENDING)
                .proposedRole(invitationDto.getProposedRole())
                .message(invitationDto.getMessage())
                .build();

        requestRepository.save(request);

        return new ResponseMessageDto("Invitation sent to worker");
    }

    // WORKER: Respond to agency invitation (accept/reject)
    @Transactional
    public ResponseMessageDto respondToInvitation(String requestId, RespondToRequestDto respondDto) {
        var currentUser = permissionService.getLoggedInUser();
        Worker worker = workerRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Worker account not found"));

        UUID id = UUID.fromString(requestId);
        WorkerAgencyRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: " + requestId));

        // Verify this is an invitation to the current worker
        if (!request.getWorker().getWorkerId().equals(worker.getWorkerId())) {
            throw new AuthorizationException("This invitation is not for you");
        }

        if (request.getRequestType() != RequestType.AGENCY_INVITATION) {
            throw new CustomDataIntegrityViolationException("This is not an agency invitation");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new CustomDataIntegrityViolationException("This invitation has already been responded to");
        }

        if (respondDto.getAccept()) {
            // Accept invitation
            acceptRequest(request, respondDto.getResponseMessage());
            return new ResponseMessageDto("You have joined " + request.getAgency().getAgencyName());
        } else {
            // Reject invitation
            request.setStatus(RequestStatus.REJECTED);
            request.setRespondedAt(LocalDateTime.now());
            request.setResponseMessage(respondDto.getResponseMessage());
            requestRepository.save(request);
            return new ResponseMessageDto("Invitation rejected");
        }
    }

    // AGENCY: Respond to worker request (accept/reject)
    @Transactional
    public ResponseMessageDto respondToWorkerRequest(String requestId, RespondToRequestDto respondDto) {
        var currentUser = permissionService.getLoggedInUser();
        Agency agency = agencyRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Agency account not found"));

        // Check if user has permission (ADMIN or MANAGER)
        AgencyMember member = agencyMemberRepository.findByAgencyAndCoreUser(agency, currentUser)
                .orElseThrow(() -> new AuthorizationException("You are not a member of this agency"));

        if (member.getAgencyRole() != AgencyRole.AGENCY_ADMIN &&
                member.getAgencyRole() != AgencyRole.AGENCY_MANAGER) {
            throw new AuthorizationException("Only agency admins and managers can respond to worker requests");
        }

        UUID id = UUID.fromString(requestId);
        WorkerAgencyRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: " + requestId));

        // Verify this request is for the current agency
        if (!request.getAgency().getAgencyId().equals(agency.getAgencyId())) {
            throw new AuthorizationException("This request is not for your agency");
        }

        if (request.getRequestType() != RequestType.WORKER_REQUEST) {
            throw new CustomDataIntegrityViolationException("This is not a worker request");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new CustomDataIntegrityViolationException("This request has already been responded to");
        }

        if (respondDto.getAccept()) {
            // Accept request
            acceptRequest(request, respondDto.getResponseMessage());
            return new ResponseMessageDto("Worker has been added to your agency");
        } else {
            // Reject request
            request.setStatus(RequestStatus.REJECTED);
            request.setRespondedAt(LocalDateTime.now());
            request.setResponseMessage(respondDto.getResponseMessage());
            requestRepository.save(request);
            return new ResponseMessageDto("Worker request rejected");
        }
    }

    // Helper method to accept a request (both invitation and worker request)
    private void acceptRequest(WorkerAgencyRequest request, String responseMessage) {
        Worker worker = request.getWorker();
        Agency agency = request.getAgency();

        // Update request status
        request.setStatus(RequestStatus.ACCEPTED);
        request.setRespondedAt(LocalDateTime.now());
        request.setResponseMessage(responseMessage);
        requestRepository.save(request);

        // Link worker to agency
        worker.setAgency(agency);
        workerRepository.save(worker);

        // Create agency member entry
        AgencyMember agencyMember = AgencyMember.builder()
                .agency(agency)
                .coreUser(worker.getCoreUser())
                .agencyRole(request.getProposedRole())
                .build();
        agencyMemberRepository.save(agencyMember);

        // Add ROLE_AGENCY to worker's user if not present
        if (!worker.getCoreUser().getUserData().getUserRoles().contains(UserRole.ROLE_AGENCY)) {
            worker.getCoreUser().getUserData().getUserRoles().add(UserRole.ROLE_AGENCY);
        }

        // Update agency total workers count
        agency.setTotalWorkers(agency.getTotalWorkers() + 1);
        agencyRepository.save(agency);
    }

    // Get pending invitations for current worker
    public List<WorkerAgencyRequestDto> getMyPendingInvitations() {
        var currentUser = permissionService.getLoggedInUser();
        Worker worker = workerRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Worker account not found"));

        List<WorkerAgencyRequest> invitations = requestRepository.findByWorkerAndStatus(worker, RequestStatus.PENDING)
                .stream()
                .filter(req -> req.getRequestType() == RequestType.AGENCY_INVITATION)
                .collect(Collectors.toList());

        return invitations.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Get pending worker requests for current agency
    public List<WorkerAgencyRequestDto> getAgencyPendingRequests() {
        var currentUser = permissionService.getLoggedInUser();
        Agency agency = agencyRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Agency account not found"));

        List<WorkerAgencyRequest> requests = requestRepository.findByAgencyAndStatus(agency, RequestStatus.PENDING)
                .stream()
                .filter(req -> req.getRequestType() == RequestType.WORKER_REQUEST)
                .collect(Collectors.toList());

        return requests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Get agency's sent invitations
    public List<WorkerAgencyRequestDto> getAgencySentInvitations() {
        var currentUser = permissionService.getLoggedInUser();
        Agency agency = agencyRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Agency account not found"));

        List<WorkerAgencyRequest> invitations = requestRepository.findByAgency(agency)
                .stream()
                .filter(req -> req.getRequestType() == RequestType.AGENCY_INVITATION)
                .collect(Collectors.toList());

        return invitations.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Get worker's sent requests
    public List<WorkerAgencyRequestDto> getMyJoinRequests() {
        var currentUser = permissionService.getLoggedInUser();
        Worker worker = workerRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Worker account not found"));

        List<WorkerAgencyRequest> requests = requestRepository.findByWorker(worker)
                .stream()
                .filter(req -> req.getRequestType() == RequestType.WORKER_REQUEST)
                .collect(Collectors.toList());

        return requests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Helper method to map entity to DTO
    private WorkerAgencyRequestDto mapToDto(WorkerAgencyRequest request) {
        Worker worker = request.getWorker();
        Agency agency = request.getAgency();
        String workerName = worker.getCoreUser().getFirstName() + " " +
                worker.getCoreUser().getLastName();

        return WorkerAgencyRequestDto.builder()
                .requestId(request.getRequestId().toString())
                .workerId(worker.getWorkerId().toString())
                .workerName(workerName)
                .workerType(worker.getWorkerType().getDisplayName())
                .agencyId(agency.getAgencyId().toString())
                .agencyName(agency.getAgencyName())
                .requestType(request.getRequestType())
                .status(request.getStatus())
                .proposedRole(request.getProposedRole())
                .message(request.getMessage())
                .responseMessage(request.getResponseMessage())
                .createdAt(request.getCreatedAt())
                .respondedAt(request.getRespondedAt())
                .build();
    }
}
