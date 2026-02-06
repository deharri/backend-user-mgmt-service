package com.deharri.ums.worker;

import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.worker.dto.request.CreateWorkerAccountDto;
import com.deharri.ums.worker.dto.request.UpdateWorkerProfileDto;
import com.deharri.ums.worker.dto.response.WorkerListItemDto;
import com.deharri.ums.worker.dto.response.WorkerProfileResponseDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

}
