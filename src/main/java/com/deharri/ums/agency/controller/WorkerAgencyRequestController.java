package com.deharri.ums.agency.controller;

import com.deharri.ums.agency.WorkerAgencyRequestService;
import com.deharri.ums.agency.dto.request.RespondToRequestDto;
import com.deharri.ums.agency.dto.request.SendInvitationDto;
import com.deharri.ums.agency.dto.response.WorkerAgencyRequestDto;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.worker.dto.request.JoinAgencyRequestDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/worker-agency-requests")
public class WorkerAgencyRequestController {

    private final WorkerAgencyRequestService requestService;

    // WORKER ENDPOINTS

    @PostMapping("/worker/join-request")
    public ResponseEntity<ResponseMessageDto> requestToJoinAgency(
            @Valid @RequestBody JoinAgencyRequestDto joinRequestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(requestService.requestToJoinAgency(joinRequestDto));
    }

    @GetMapping("/worker/my-requests")
    public ResponseEntity<List<WorkerAgencyRequestDto>> getMyJoinRequests() {
        return ResponseEntity.ok(requestService.getMyJoinRequests());
    }

    @GetMapping("/worker/invitations")
    public ResponseEntity<List<WorkerAgencyRequestDto>> getMyPendingInvitations() {
        return ResponseEntity.ok(requestService.getMyPendingInvitations());
    }

    @PostMapping("/worker/invitations/{requestId}/respond")
    public ResponseEntity<ResponseMessageDto> respondToInvitation(
            @PathVariable String requestId,
            @Valid @RequestBody RespondToRequestDto respondDto
    ) {
        return ResponseEntity.ok(requestService.respondToInvitation(requestId, respondDto));
    }

    // AGENCY ENDPOINTS

    @PostMapping("/agency/send-invitation")
    public ResponseEntity<ResponseMessageDto> sendInvitationToWorker(
            @Valid @RequestBody SendInvitationDto invitationDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(requestService.sendInvitationToWorker(invitationDto));
    }

    @GetMapping("/agency/pending-requests")
    public ResponseEntity<List<WorkerAgencyRequestDto>> getAgencyPendingRequests() {
        return ResponseEntity.ok(requestService.getAgencyPendingRequests());
    }

    @GetMapping("/agency/sent-invitations")
    public ResponseEntity<List<WorkerAgencyRequestDto>> getAgencySentInvitations() {
        return ResponseEntity.ok(requestService.getAgencySentInvitations());
    }

    @PostMapping("/agency/requests/{requestId}/respond")
    public ResponseEntity<ResponseMessageDto> respondToWorkerRequest(
            @PathVariable String requestId,
            @Valid @RequestBody RespondToRequestDto respondDto
    ) {
        return ResponseEntity.ok(requestService.respondToWorkerRequest(requestId, respondDto));
    }

}
