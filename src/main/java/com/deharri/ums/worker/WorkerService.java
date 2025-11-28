package com.deharri.ums.worker;

import com.deharri.ums.amazon.S3Service;
import com.deharri.ums.error.exception.AuthorizationException;
import com.deharri.ums.permission.PermissionService;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.worker.dto.request.CreateWorkerAccountDto;
import com.deharri.ums.worker.dto.response.WorkerTypeDto;
import com.deharri.ums.worker.entity.Worker;
import com.deharri.ums.worker.mapper.WorkerMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final WorkerMapper workerMapper;
    private final PermissionService permissionService;
    private final S3Service s3Service;

    @Transactional
    public ResponseMessageDto createWorkerAccount(CreateWorkerAccountDto dto) {
        Worker worker = workerMapper.createWorkerAccountDtoToWorker(dto);
        workerRepository.save(worker);
        return new ResponseMessageDto("Worker account created successfully");
    }

    public List<WorkerTypeDto> getAllWorkerTypes() {
        return Worker.WorkerType.getAllTypes();
    }

    @Transactional
    public String submitCnicForVerification(MultipartFile cnicFront, MultipartFile cnicBack) {
        var currentUser = permissionService.getLoggedInUser();
        String frontPath = s3Service.generateFileKey(currentUser.getUserId(), Objects.requireNonNull(cnicFront.getOriginalFilename()));
        String backPath = s3Service.generateFileKey(currentUser.getUserId(), Objects.requireNonNull(cnicBack.getOriginalFilename()));
        s3Service.uploadFile(cnicFront, frontPath);
        s3Service.uploadFile(cnicBack, backPath);
        var currentWorker = workerRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Worker account not found for given user"));
        currentWorker.getCnicVerification().setCnicFrontPath(frontPath);
        currentWorker.getCnicVerification().setCnicBackPath(backPath);
        workerRepository.save(currentWorker);
        return "CNIC submitted for verification successfully";
    }
}
