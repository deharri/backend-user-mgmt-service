package com.deharri.ums.worker;

import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.worker.dto.request.CreateWorkerAccountDto;
import com.deharri.ums.worker.dto.request.UpdateAvailabilityDto;
import com.deharri.ums.worker.dto.request.UpdateWorkerProfileDto;
import com.deharri.ums.worker.dto.response.WorkerListItemDto;
import com.deharri.ums.worker.dto.response.WorkerProfileResponseDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/workers")
public class WorkerController {

    private final WorkerService workerService;

    @PostMapping("/create")
    public ResponseEntity<ResponseMessageDto> createWorkerAccount(
            @Valid @RequestBody CreateWorkerAccountDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workerService.createWorkerAccount(dto));
    }
    
    @PostMapping("/verify/cnic")
    public ResponseEntity<ResponseMessageDto> submitCnicForVerification(
            @RequestPart MultipartFile cnicFront,
            @RequestPart MultipartFile cnicBack
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessageDto(workerService.submitCnicForVerification(cnicFront, cnicBack)));
    }

    @GetMapping
    public ResponseEntity<List<WorkerListItemDto>> getAllWorkers() {
        return ResponseEntity.ok(workerService.getAllWorkers());
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<WorkerProfileResponseDto> getWorkerByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(workerService.getWorkerByUserId(userId));
    }

    @GetMapping("/{workerId}")
    public ResponseEntity<WorkerProfileResponseDto> getWorkerById(@PathVariable String workerId) {
        return ResponseEntity.ok(workerService.getWorkerById(workerId));
    }

    @GetMapping("/me")
    public ResponseEntity<WorkerProfileResponseDto> getMyWorkerProfile() {
        return ResponseEntity.ok(workerService.getMyWorkerProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<ResponseMessageDto> updateMyWorkerProfile(
            @Valid @RequestBody UpdateWorkerProfileDto updateDto
    ) {
        return ResponseEntity.ok(workerService.updateMyWorkerProfile(updateDto));
    }

    @PutMapping("/me/availability")
    public ResponseEntity<WorkerProfileResponseDto> updateAvailability(
            @Valid @RequestBody UpdateAvailabilityDto dto
    ) {
        return ResponseEntity.ok(workerService.updateAvailability(dto));
    }

    @PostMapping("/portfolio/upload")
    public ResponseEntity<ResponseMessageDto> uploadPortfolioImage(
            @RequestPart MultipartFile portfolioImage
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workerService.uploadPortfolioImage(portfolioImage));
    }

    @DeleteMapping("/portfolio")
    public ResponseEntity<ResponseMessageDto> deletePortfolioImage(
            @RequestParam String imagePath
    ) {
        return ResponseEntity.ok(workerService.deletePortfolioImage(imagePath));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<WorkerListItemDto>> getNearbyWorkers(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radiusKm,
            @RequestParam(required = false) String workerType) {
        return ResponseEntity.ok(workerService.getNearbyWorkers(lat, lng, radiusKm, workerType));
    }

    @PutMapping("/internal/{workerId}/subscription/activate")
    public ResponseEntity<Void> activateSubscription(
            @PathVariable UUID workerId,
            @RequestBody Map<String, String> body) {
        LocalDateTime expiresAt = LocalDateTime.parse(body.get("expiresAt"));
        workerService.activateSubscription(workerId, expiresAt);
        return ResponseEntity.ok().build();
    }

}
