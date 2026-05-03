package com.deharri.ums.agency.controller;

import com.deharri.ums.agency.AgencyService;
import com.deharri.ums.agency.dto.request.CreateAgencyDto;
import com.deharri.ums.agency.dto.request.UpdateAgencyDto;
import com.deharri.ums.agency.dto.response.AgencyListItemDto;
import com.deharri.ums.agency.dto.response.AgencyProfileResponseDto;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.worker.dto.response.WorkerListItemDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/agencies")
public class AgencyController {

    private final AgencyService agencyService;

    @PostMapping("/create")
    public ResponseEntity<ResponseMessageDto> createAgency(
            @Valid @RequestBody CreateAgencyDto createAgencyDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agencyService.createAgency(createAgencyDto));
    }

    @GetMapping
    public ResponseEntity<List<AgencyListItemDto>> getAllAgencies() {
        return ResponseEntity.ok(agencyService.getAllAgencies());
    }

    @GetMapping("/{agencyId}")
    public ResponseEntity<AgencyProfileResponseDto> getAgencyById(@PathVariable String agencyId) {
        return ResponseEntity.ok(agencyService.getAgencyById(agencyId));
    }

    @GetMapping("/me")
    public ResponseEntity<AgencyProfileResponseDto> getMyAgencyProfile() {
        return ResponseEntity.ok(agencyService.getMyAgencyProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<ResponseMessageDto> updateMyAgencyProfile(
            @Valid @RequestBody UpdateAgencyDto updateDto
    ) {
        return ResponseEntity.ok(agencyService.updateMyAgencyProfile(updateDto));
    }

    @PostMapping("/license/upload")
    public ResponseEntity<ResponseMessageDto> uploadLicenseDocument(
            @RequestPart MultipartFile licenseDocument
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agencyService.uploadLicenseDocument(licenseDocument));
    }

    @PostMapping("/workers/{workerId}/invite")
    public ResponseEntity<ResponseMessageDto> inviteWorker(@PathVariable String workerId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agencyService.inviteWorkerToAgency(workerId));
    }

    @GetMapping("/invitations/my")
    public ResponseEntity<java.util.List<com.deharri.ums.agency.dto.response.WorkerInvitationDto>> getMyInvitations() {
        return ResponseEntity.ok(agencyService.getMyPendingInvitations());
    }

    @PutMapping("/invitations/{invitationId}/respond")
    public ResponseEntity<ResponseMessageDto> respondToInvitation(
            @PathVariable String invitationId,
            @RequestParam("accept") boolean accept) {
        return ResponseEntity.ok(agencyService.respondToInvitation(invitationId, accept));
    }

    @DeleteMapping("/workers/{workerId}")
    public ResponseEntity<ResponseMessageDto> removeWorker(@PathVariable String workerId) {
        return ResponseEntity.ok(agencyService.removeWorkerFromAgency(workerId));
    }

    @GetMapping("/{agencyId}/workers")
    public ResponseEntity<List<WorkerListItemDto>> getAgencyWorkers(@PathVariable String agencyId) {
        return ResponseEntity.ok(agencyService.listAgencyWorkers(agencyId));
    }

    /** Worker-initiated leave from their current agency. Required to switch agencies or become an owner. */
    @DeleteMapping("/me/membership")
    public ResponseEntity<ResponseMessageDto> leaveCurrentAgency() {
        return ResponseEntity.ok(agencyService.leaveCurrentAgency());
    }

    /**
     * Returns the membership history of the calling user's agency — current and past members
     * with joined/left timestamps and final status. Caller must own an agency.
     */
    @GetMapping("/me/members/history")
    public ResponseEntity<List<com.deharri.ums.agency.dto.response.AgencyMembershipHistoryDto>> getMyMembersHistory() {
        return ResponseEntity.ok(agencyService.getMyAgencyMembersHistory());
    }

    /** Returns invitations sent by the calling user's agency — pending, accepted, rejected. */
    @GetMapping("/me/invitations")
    public ResponseEntity<List<com.deharri.ums.agency.dto.response.AgencySentInvitationDto>> getMyAgencyInvitations() {
        return ResponseEntity.ok(agencyService.getMyAgencyInvitations());
    }
}
