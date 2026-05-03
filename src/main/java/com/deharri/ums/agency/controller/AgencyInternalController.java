package com.deharri.ums.agency.controller;

import com.deharri.ums.agency.AgencyMemberRepository;
import com.deharri.ums.agency.AgencyRepository;
import com.deharri.ums.agency.dto.request.UpdateAgencyStatsDto;
import com.deharri.ums.agency.dto.response.InternalAgencyDto;
import com.deharri.ums.agency.entity.Agency;
import com.deharri.ums.agency.entity.AgencyMember;
import com.deharri.ums.enums.AgencyRole;
import com.deharri.ums.enums.AgencySubscriptionStatus;
import com.deharri.ums.enums.UserRole;
import com.deharri.ums.error.exception.ResourceNotFoundException;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.user.entity.CoreUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/v1/agencies/internal")
public class AgencyInternalController {

    private final AgencyRepository agencyRepository;
    private final AgencyMemberRepository agencyMemberRepository;

    @GetMapping("/{agencyId}")
    @Transactional(readOnly = true)
    public ResponseEntity<InternalAgencyDto> getAgencyById(@PathVariable String agencyId) {
        UUID id = UUID.fromString(agencyId);
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found: " + agencyId));
        CoreUser owner = agency.getCoreUser();
        return ResponseEntity.ok(InternalAgencyDto.builder()
                .agencyId(agency.getAgencyId())
                .agencyName(agency.getAgencyName())
                .ownerUserId(owner != null ? owner.getUserId() : null)
                .ownerUsername(owner != null ? owner.getUsername() : null)
                .subscriptionActive(agency.isSubscriptionActive())
                .build());
    }

    @GetMapping("/{agencyId}/workers/{workerId}/membership")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Boolean>> isWorkerInAgency(
            @PathVariable String agencyId,
            @PathVariable String workerId) {
        UUID aId = UUID.fromString(agencyId);
        UUID id = UUID.fromString(workerId);
        Agency agency = agencyRepository.findById(aId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found: " + agencyId));
        // Defensive: callers historically passed either the user-id or the worker-entity id.
        // Match against both so the check is robust to either convention.
        boolean member = agency.getWorkers() != null && agency.getWorkers().stream()
                .anyMatch(w -> id.equals(w.getWorkerId())
                        || (w.getCoreUser() != null && id.equals(w.getCoreUser().getUserId())));
        return ResponseEntity.ok(Map.of("member", member));
    }

    @PutMapping("/{agencyId}/subscription/activate")
    @Transactional
    public ResponseEntity<ResponseMessageDto> activateSubscription(
            @PathVariable String agencyId,
            @RequestBody Map<String, String> body
    ) {
        UUID id = UUID.fromString(agencyId);
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found: " + agencyId));

        LocalDateTime newExpiry = LocalDateTime.parse(body.get("expiresAt"));

        if (agency.getSubscriptionStartedAt() == null) {
            agency.setSubscriptionStartedAt(LocalDateTime.now());
        }
        agency.setSubscriptionStatus(AgencySubscriptionStatus.ACTIVE);
        agency.setSubscriptionExpiresAt(newExpiry);

        if (agency.getCoreUser() != null && agency.getCoreUser().getUserData() != null
                && !agency.getCoreUser().getUserData().getUserRoles().contains(UserRole.ROLE_AGENCY)) {
            agency.getCoreUser().getUserData().getUserRoles().add(UserRole.ROLE_AGENCY);
        }

        boolean alreadyMember = agencyMemberRepository
                .findByAgencyAndCoreUser(agency, agency.getCoreUser()).isPresent();
        if (!alreadyMember) {
            agencyMemberRepository.save(AgencyMember.builder()
                    .agency(agency)
                    .coreUser(agency.getCoreUser())
                    .agencyRole(AgencyRole.AGENCY_ADMIN)
                    .build());
        }

        agencyRepository.save(agency);
        return ResponseEntity.ok(new ResponseMessageDto("Agency subscription activated until " + newExpiry));
    }

    @PutMapping("/{agencyId}/stats")
    @Transactional
    public ResponseEntity<Void> updateAgencyStats(
            @PathVariable String agencyId,
            @RequestBody UpdateAgencyStatsDto statsDto) {
        UUID id = UUID.fromString(agencyId);
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found: " + agencyId));

        if (statsDto.getAverageRating() != null) {
            agency.setAverageRating(statsDto.getAverageRating());
        }
        if (statsDto.getTotalJobsCompleted() != null) {
            agency.setTotalJobsCompleted(statsDto.getTotalJobsCompleted());
        }
        agencyRepository.save(agency);
        log.info("Updated stats for agency {}: rating={}, jobsCompleted={}",
                agencyId, statsDto.getAverageRating(), statsDto.getTotalJobsCompleted());
        return ResponseEntity.ok().build();
    }
}
