package com.deharri.ums.worker.controller.internal;

import com.deharri.ums.enums.Language;
import com.deharri.ums.enums.PakistanCity;
import com.deharri.ums.error.exception.ResourceNotFoundException;
import com.deharri.ums.worker.WorkerRepository;
import com.deharri.ums.worker.dto.request.UpdateWorkerStatsDto;
import com.deharri.ums.worker.dto.response.InternalWorkerProfileDto;
import com.deharri.ums.worker.entity.Worker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/workers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Internal Worker API", description = "Internal endpoints for inter-service communication")
public class InternalWorkerController {

    private final WorkerRepository workerRepository;

    @GetMapping
    @Operation(summary = "Get all workers (for bulk sync)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<InternalWorkerProfileDto>> getAllWorkers() {
        List<Worker> workers = workerRepository.findAll();
        List<InternalWorkerProfileDto> dtos = workers.stream()
                .map(this::toInternalDto)
                .toList();
        log.info("Internal API: Returning {} worker profiles for sync", dtos.size());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{workerId}")
    @Operation(summary = "Get single worker (for single sync)")
    @Transactional(readOnly = true)
    public ResponseEntity<InternalWorkerProfileDto> getWorkerById(@PathVariable UUID workerId) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found: " + workerId));
        return ResponseEntity.ok(toInternalDto(worker));
    }

    @PutMapping("/{workerId}/stats")
    @Operation(summary = "Update worker rating and jobs completed stats")
    @Transactional
    public ResponseEntity<Void> updateWorkerStats(
            @PathVariable UUID workerId,
            @RequestBody UpdateWorkerStatsDto statsDto) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found: " + workerId));

        if (statsDto.getAverageRating() != null) {
            worker.setAverageRating(statsDto.getAverageRating());
        }
        if (statsDto.getTotalJobsCompleted() != null) {
            worker.setTotalJobsCompleted(statsDto.getTotalJobsCompleted());
        }

        workerRepository.save(worker);
        log.info("Updated stats for worker {}: rating={}, jobsCompleted={}",
                workerId, statsDto.getAverageRating(), statsDto.getTotalJobsCompleted());
        return ResponseEntity.ok().build();
    }

    private InternalWorkerProfileDto toInternalDto(Worker worker) {
        return InternalWorkerProfileDto.builder()
                .workerId(worker.getWorkerId())
                .userId(worker.getCoreUser().getUserId())
                .username(worker.getCoreUser().getUsername())
                .firstName(worker.getCoreUser().getFirstName())
                .lastName(worker.getCoreUser().getLastName())
                .profilePicturePath(worker.getCoreUser().getUserData() != null
                        ? worker.getCoreUser().getUserData().getProfilePicturePath() : null)
                .workerType(worker.getWorkerType() != null ? worker.getWorkerType().name() : null)
                .skills(worker.getSkills())
                .bio(worker.getBio())
                .experienceYears(worker.getExperienceYears())
                .hourlyRate(worker.getHourlyRate())
                .dailyRate(worker.getDailyRate())
                .city(worker.getCity() != null ? worker.getCity().name() : null)
                .area(worker.getArea())
                .serviceCities(worker.getServiceCities() != null
                        ? worker.getServiceCities().stream().map(PakistanCity::name).toList()
                        : List.of())
                .languages(worker.getLanguages() != null
                        ? worker.getLanguages().stream().map(Language::name).toList()
                        : List.of())
                .availabilityStatus(worker.getAvailabilityStatus() != null
                        ? worker.getAvailabilityStatus().getAvailabilityStatus().name() : null)
                .isVerified(worker.isVerified())
                .averageRating(worker.getAverageRating())
                .totalJobsCompleted(worker.getTotalJobsCompleted())
                .agencyName(worker.getAgency() != null ? worker.getAgency().getAgencyName() : null)
                .build();
    }
}
