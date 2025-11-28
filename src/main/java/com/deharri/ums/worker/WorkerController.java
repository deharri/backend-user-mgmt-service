package com.deharri.ums.worker;

import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.worker.dto.request.CreateWorkerAccountDto;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/workers")
public class WorkerController {

    private final WorkerService workerService;

    @PostMapping("/create")
    public ResponseEntity<ResponseMessageDto> createWorkerAccount(
            @RequestBody CreateWorkerAccountDto dto
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

}
