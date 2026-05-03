package com.deharri.ums.agency;

import com.deharri.ums.agency.dto.request.CreateAgencyDto;
import com.deharri.ums.agency.dto.request.UpdateAgencyDto;
import com.deharri.ums.agency.dto.response.AgencyListItemDto;
import com.deharri.ums.agency.dto.response.AgencyMembershipHistoryDto;
import com.deharri.ums.agency.dto.response.AgencyProfileResponseDto;
import com.deharri.ums.agency.dto.response.AgencySentInvitationDto;
import com.deharri.ums.agency.dto.response.WorkerInvitationDto;
import com.deharri.ums.agency.entity.Agency;
import com.deharri.ums.agency.entity.AgencyMember;
import com.deharri.ums.agency.entity.WorkerAgencyInvitation;
import com.deharri.ums.agency.mapper.AgencyMapper;
import com.deharri.ums.amazon.S3Service;
import com.deharri.ums.error.exception.AuthorizationException;
import com.deharri.ums.error.exception.CustomDataIntegrityViolationException;
import com.deharri.ums.error.exception.ResourceNotFoundException;
import com.deharri.ums.permission.PermissionService;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.worker.WorkerRepository;
import com.deharri.ums.worker.dto.response.WorkerListItemDto;
import com.deharri.ums.worker.mapper.WorkerMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AgencyService {

    private final AgencyRepository agencyRepository;
    private final AgencyMemberRepository agencyMemberRepository;
    private final AgencyMapper agencyMapper;
    private final PermissionService permissionService;
    private final S3Service s3Service;
    private final WorkerRepository workerRepository;
    private final WorkerMapper workerMapper;
    private final WorkerAgencyInvitationRepository invitationRepository;

    @Transactional
    public ResponseMessageDto createAgency(CreateAgencyDto createAgencyDto) {
        if (agencyRepository.existsByAgencyName(createAgencyDto.getAgencyName())) {
            throw new CustomDataIntegrityViolationException("Agency with this name already exists");
        }
        var currentUser = permissionService.getLoggedInUser();

        if (agencyRepository.findByCoreUser(currentUser).isPresent()) {
            throw new CustomDataIntegrityViolationException("You already own an agency");
        }

        // Workers who currently belong to an agency must leave first before creating their own.
        // This keeps the "one active agency relationship per worker" rule consistent.
        workerRepository.findByCoreUser_UserId(currentUser.getUserId())
                .ifPresent(w -> {
                    if (w.getAgency() != null) {
                        throw new CustomDataIntegrityViolationException(
                                "You are currently a worker in agency '" + w.getAgency().getAgencyName()
                                        + "'. Leave it first before creating your own agency.");
                    }
                });

        Agency agency = agencyMapper.createAgencyDtoToAgency(createAgencyDto);
        agency.setCoreUser(currentUser);
        agencyRepository.save(agency);

        return new ResponseMessageDto("Agency created in INACTIVE state. Pay subscription to activate. Agency ID: "
                + agency.getAgencyId());
    }

    public List<AgencyListItemDto> getAllAgencies() {
        return agencyRepository.findAll().stream()
                .filter(Agency::isSubscriptionActive)
                .map(agencyMapper::agencyToListItemDto)
                .collect(Collectors.toList());
    }

    public AgencyProfileResponseDto getAgencyById(String agencyId) {
        UUID id = UUID.fromString(agencyId);
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found with ID: " + agencyId));
        if (!agency.isSubscriptionActive()) {
            throw new ResourceNotFoundException("Agency subscription expired or inactive");
        }
        return agencyMapper.agencyToProfileResponseDto(agency);
    }

    public AgencyProfileResponseDto getMyAgencyProfile() {
        var currentUser = permissionService.getLoggedInUser();
        Agency agency = agencyRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Agency account not found for current user"));
        return agencyMapper.agencyToProfileResponseDto(agency);
    }

    @Transactional
    public ResponseMessageDto updateMyAgencyProfile(UpdateAgencyDto updateDto) {
        var currentUser = permissionService.getLoggedInUser();
        Agency agency = agencyRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Agency account not found for current user"));
        if (!agency.isSubscriptionActive()) {
            throw new AuthorizationException("Agency subscription is inactive. Renew to continue.");
        }

        // Check if new agency name conflicts with existing one
        if (updateDto.getAgencyName() != null &&
                !updateDto.getAgencyName().equals(agency.getAgencyName()) &&
                agencyRepository.existsByAgencyName(updateDto.getAgencyName())) {
            throw new CustomDataIntegrityViolationException("Agency with this name already exists");
        }

        agencyMapper.updateAgencyFromDto(updateDto, agency);
        agencyRepository.save(agency);

        return new ResponseMessageDto("Agency profile updated successfully");
    }

    @Transactional
    public ResponseMessageDto uploadLicenseDocument(MultipartFile licenseDocument) {
        var currentUser = permissionService.getLoggedInUser();
        Agency agency = agencyRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Agency account not found for current user"));
        if (!agency.isSubscriptionActive()) {
            throw new AuthorizationException("Agency subscription is inactive. Renew to continue.");
        }

        // Delete old license document if exists
        if (agency.getLicensePath() != null && !agency.getLicensePath().isBlank()) {
            s3Service.deleteFile(agency.getLicensePath());
        }

        String licensePath = s3Service.generateFileKey(
                currentUser.getUserId(),
                Objects.requireNonNull(licenseDocument.getOriginalFilename())
        );
        s3Service.uploadFile(licenseDocument, licensePath);

        agency.setLicensePath(licensePath);
        agencyRepository.save(agency);

        return new ResponseMessageDto("License document uploaded successfully");
    }

    @Transactional
    public ResponseMessageDto removeWorkerFromAgency(String workerId) {
        var currentUser = permissionService.getLoggedInUser();
        Agency agency = agencyRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Agency not found"));
        if (!agency.isSubscriptionActive()) {
            throw new AuthorizationException("Agency subscription is inactive. Renew to continue.");
        }

        UUID wId = UUID.fromString(workerId);
        com.deharri.ums.worker.entity.Worker worker = workerRepository.findById(wId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found: " + workerId));

        if (worker.getAgency() == null || !worker.getAgency().getAgencyId().equals(agency.getAgencyId())) {
            throw new CustomDataIntegrityViolationException("Worker is not a member of your agency");
        }

        worker.setAgency(null);
        workerRepository.save(worker);

        // Soft-end the membership so we keep the history record (joinedAt/leftAt/status).
        agencyMemberRepository.findByAgencyAndCoreUser(agency, worker.getCoreUser())
                .ifPresent(member -> {
                    if (member.getMembershipStatus() == AgencyMember.MembershipStatus.ACTIVE) {
                        member.setMembershipStatus(AgencyMember.MembershipStatus.REMOVED);
                        member.setLeftAt(java.time.Instant.now());
                        agencyMemberRepository.save(member);
                    }
                });

        agency.setTotalWorkers(Math.max(0, agency.getTotalWorkers() - 1));
        agencyRepository.save(agency);

        return new ResponseMessageDto("Worker removed from agency");
    }

    /**
     * Worker-initiated departure from their current agency. Required before they can:
     *  - accept an invitation from a different agency, or
     *  - become an agency owner themselves.
     * Keeps the agency_member row but marks status=LEFT with leftAt=now for history.
     */
    @Transactional
    public ResponseMessageDto leaveCurrentAgency() {
        var currentUser = permissionService.getLoggedInUser();
        com.deharri.ums.worker.entity.Worker worker = workerRepository
                .findByCoreUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("You don't have a worker profile"));

        Agency agency = worker.getAgency();
        if (agency == null) {
            throw new CustomDataIntegrityViolationException("You are not currently in any agency");
        }

        worker.setAgency(null);
        workerRepository.save(worker);

        agencyMemberRepository.findByAgencyAndCoreUser(agency, worker.getCoreUser())
                .ifPresent(member -> {
                    if (member.getMembershipStatus() == AgencyMember.MembershipStatus.ACTIVE) {
                        member.setMembershipStatus(AgencyMember.MembershipStatus.LEFT);
                        member.setLeftAt(java.time.Instant.now());
                        agencyMemberRepository.save(member);
                    }
                });

        agency.setTotalWorkers(Math.max(0, agency.getTotalWorkers() - 1));
        agencyRepository.save(agency);

        return new ResponseMessageDto("You have left " + agency.getAgencyName());
    }

    @Transactional
    public ResponseMessageDto inviteWorkerToAgency(String workerId) {
        var currentUser = permissionService.getLoggedInUser();
        Agency agency = agencyRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Agency not found"));
        if (!agency.isSubscriptionActive()) {
            throw new AuthorizationException("Agency subscription is inactive. Renew to continue.");
        }

        UUID wId = UUID.fromString(workerId);
        com.deharri.ums.worker.entity.Worker worker = workerRepository.findById(wId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found: " + workerId));

        if (worker.getAgency() != null
                && worker.getAgency().getAgencyId().equals(agency.getAgencyId())) {
            return new ResponseMessageDto("Worker is already in your agency");
        }

        if (worker.getAgency() != null) {
            throw new CustomDataIntegrityViolationException(
                    "Worker already belongs to agency: " + worker.getAgency().getAgencyName());
        }

        // Check for an existing pending invitation from this agency to this worker
        if (invitationRepository
                .findByWorkerAndAgencyAndStatus(worker, agency, WorkerAgencyInvitation.Status.PENDING)
                .isPresent()) {
            return new ResponseMessageDto("Invitation already pending for this worker");
        }

        WorkerAgencyInvitation invite = WorkerAgencyInvitation.builder()
                .worker(worker)
                .agency(agency)
                .status(WorkerAgencyInvitation.Status.PENDING)
                .build();
        invitationRepository.save(invite);
        return new ResponseMessageDto("Invitation sent to worker");
    }

    @Transactional(readOnly = true)
    public List<WorkerInvitationDto> getMyPendingInvitations() {
        var currentUser = permissionService.getLoggedInUser();
        com.deharri.ums.worker.entity.Worker worker = workerRepository.findByCoreUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("You don't have a worker profile"));
        return invitationRepository
                .findByWorkerAndStatusOrderByCreatedAtDesc(worker, WorkerAgencyInvitation.Status.PENDING)
                .stream()
                .map(inv -> WorkerInvitationDto.builder()
                        .invitationId(inv.getInvitationId())
                        .agencyId(inv.getAgency().getAgencyId())
                        .agencyName(inv.getAgency().getAgencyName())
                        .status(inv.getStatus().name())
                        .createdAt(inv.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public ResponseMessageDto respondToInvitation(String invitationId, boolean accept) {
        var currentUser = permissionService.getLoggedInUser();
        UUID invId = UUID.fromString(invitationId);
        WorkerAgencyInvitation invite = invitationRepository.findById(invId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!invite.getWorker().getCoreUser().getUserId().equals(currentUser.getUserId())) {
            throw new AuthorizationException("Only the invited worker can respond to this invitation");
        }
        if (invite.getStatus() != WorkerAgencyInvitation.Status.PENDING) {
            throw new CustomDataIntegrityViolationException("Invitation is no longer pending");
        }

        invite.setRespondedAt(java.time.Instant.now());

        if (accept) {
            // Reject if worker has joined another agency in the meantime
            if (invite.getWorker().getAgency() != null) {
                invite.setStatus(WorkerAgencyInvitation.Status.REJECTED);
                invitationRepository.save(invite);
                throw new CustomDataIntegrityViolationException(
                        "You are already in another agency. Leave first to accept this invitation.");
            }
            invite.setStatus(WorkerAgencyInvitation.Status.ACCEPTED);
            Agency agency = invite.getAgency();
            com.deharri.ums.worker.entity.Worker worker = invite.getWorker();
            worker.setAgency(agency);
            workerRepository.save(worker);

            // If a prior membership row exists for this (agency, user) — they're rejoining.
            // Reuse the row, refresh joinedAt, clear leftAt, set status=ACTIVE.
            agencyMemberRepository.findByAgencyAndCoreUser(agency, worker.getCoreUser())
                    .ifPresentOrElse(existing -> {
                        existing.setJoinedAt(java.time.Instant.now());
                        existing.setLeftAt(null);
                        existing.setMembershipStatus(AgencyMember.MembershipStatus.ACTIVE);
                        agencyMemberRepository.save(existing);
                    }, () -> agencyMemberRepository.save(AgencyMember.builder()
                            .agency(agency)
                            .coreUser(worker.getCoreUser())
                            .agencyRole(com.deharri.ums.enums.AgencyRole.AGENCY_WORKER)
                            .joinedAt(java.time.Instant.now())
                            .membershipStatus(AgencyMember.MembershipStatus.ACTIVE)
                            .build()));
            agency.setTotalWorkers(agency.getTotalWorkers() + 1);
            agencyRepository.save(agency);
            invitationRepository.save(invite);
            return new ResponseMessageDto("Invitation accepted; you are now in " + agency.getAgencyName());
        } else {
            invite.setStatus(WorkerAgencyInvitation.Status.REJECTED);
            invitationRepository.save(invite);
            return new ResponseMessageDto("Invitation rejected");
        }
    }

    public List<WorkerListItemDto> listAgencyWorkers(String agencyId) {
        UUID id = UUID.fromString(agencyId);
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found: " + agencyId));
        return agency.getWorkers().stream()
                .map(workerMapper::workerToListItemDto)
                .collect(Collectors.toList());
    }

    /**
     * Returns the full membership history (current + past) of the calling user's agency.
     * Drives the "members history" admin view; per-worker analytics (jobs done, earnings)
     * are queried from job-service / payment-service separately.
     */
    @Transactional(readOnly = true)
    public List<AgencyMembershipHistoryDto> getMyAgencyMembersHistory() {
        var currentUser = permissionService.getLoggedInUser();
        Agency agency = agencyRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Agency not found"));

        return agencyMemberRepository
                .findByAgencyOrderByMembershipStatusAscJoinedAtDesc(agency)
                .stream()
                .map(this::toMembershipHistoryDto)
                .collect(Collectors.toList());
    }

    private AgencyMembershipHistoryDto toMembershipHistoryDto(AgencyMember m) {
        var u = m.getCoreUser();
        var workerOpt = workerRepository.findByCoreUser_UserId(u.getUserId());
        return AgencyMembershipHistoryDto.builder()
                .memberId(m.getMemberId())
                .userId(u.getUserId())
                .workerId(workerOpt.map(com.deharri.ums.worker.entity.Worker::getWorkerId).orElse(null))
                .username(u.getUsername())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .workerType(workerOpt.map(w -> w.getWorkerType() != null ? w.getWorkerType().name() : null).orElse(null))
                .membershipStatus(m.getMembershipStatus())
                .joinedAt(m.getJoinedAt())
                .leftAt(m.getLeftAt())
                .build();
    }

    /**
     * Returns every invitation the calling user's agency has ever sent — pending,
     * accepted, and rejected — newest first. Drives the dashboard "Invitations" view.
     */
    @Transactional(readOnly = true)
    public List<AgencySentInvitationDto> getMyAgencyInvitations() {
        var currentUser = permissionService.getLoggedInUser();
        Agency agency = agencyRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Agency not found"));

        return invitationRepository.findByAgencyOrderByCreatedAtDesc(agency).stream()
                .map(this::toSentInvitationDto)
                .collect(Collectors.toList());
    }

    private AgencySentInvitationDto toSentInvitationDto(WorkerAgencyInvitation inv) {
        var worker = inv.getWorker();
        var user = worker.getCoreUser();
        return AgencySentInvitationDto.builder()
                .invitationId(inv.getInvitationId())
                .workerId(worker.getWorkerId())
                .workerUserId(user.getUserId())
                .workerUsername(user.getUsername())
                .workerFirstName(user.getFirstName())
                .workerLastName(user.getLastName())
                .workerType(worker.getWorkerType() != null ? worker.getWorkerType().name() : null)
                .status(inv.getStatus().name())
                .createdAt(inv.getCreatedAt())
                .respondedAt(inv.getRespondedAt())
                .build();
    }
}
