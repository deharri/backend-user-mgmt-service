package com.deharri.ums.worker.controller;

import com.deharri.ums.worker.WorkerService;
import com.deharri.ums.worker.dto.response.WorkerTypeDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/public/api/v1/workers")
@AllArgsConstructor
@RestController
public class WorkerPublicController {

    private final WorkerService workerService;

    @GetMapping("/types/all")
    public ResponseEntity<List<WorkerTypeDto>> getAllWorkerTypes() {
        return ResponseEntity.ok(workerService.getAllWorkerTypes());
    }

}
