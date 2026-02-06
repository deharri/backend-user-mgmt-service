package com.deharri.ums.worker;

import com.deharri.ums.amazon.S3Service;
import com.deharri.ums.enums.UserRole;
import com.deharri.ums.error.exception.AuthorizationException;
import com.deharri.ums.error.exception.ResourceNotFoundException;
import com.deharri.ums.permission.PermissionService;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.user.entity.CoreUser;
import com.deharri.ums.worker.dto.request.CreateWorkerAccountDto;
import com.deharri.ums.worker.dto.request.UpdateWorkerProfileDto;
import com.deharri.ums.worker.dto.response.WorkerListItemDto;
import com.deharri.ums.worker.dto.response.WorkerProfileResponseDto;
import com.deharri.ums.worker.dto.response.WorkerTypeDto;
import com.deharri.ums.worker.entity.Worker;
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
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final WorkerMapper workerMapper;
    private final PermissionService permissionService;
    private final S3Service s3Service;

    @Transactional
    public ResponseMessageDto createWorkerAccount(CreateWorkerAccountDto dto) {
        Worker worker = workerMapper.createWorkerAccountDtoToWorker(dto);
        worker.getCoreUser().getUserData().getUserRoles().add(UserRole.ROLE_WORKER);
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

    public List<WorkerListItemDto> getAllWorkers() {
        return workerRepository.findAll().stream()
                .map(workerMapper::workerToListItemDto)
                .collect(Collectors.toList());
    }

    public WorkerProfileResponseDto getWorkerById(String workerId) {
        UUID id = UUID.fromString(workerId);
        Worker worker = workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found with ID: " + workerId));
        return workerMapper.workerToProfileResponseDto(worker);
    }

    public WorkerProfileResponseDto getMyWorkerProfile() {
        var currentUser = permissionService.getLoggedInUser();
        Worker worker = workerRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Worker account not found for current user"));
        return workerMapper.workerToProfileResponseDto(worker);
    }

    @Transactional
    public ResponseMessageDto updateMyWorkerProfile(UpdateWorkerProfileDto updateDto) {
        var currentUser = permissionService.getLoggedInUser();
        Worker worker = workerRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Worker account not found for current user"));

        workerMapper.updateWorkerFromDto(updateDto, worker);
        workerRepository.save(worker);

        return new ResponseMessageDto("Worker profile updated successfully");
    }

    @Transactional
    public ResponseMessageDto uploadPortfolioImage(MultipartFile portfolioImage) {
        var currentUser = permissionService.getLoggedInUser();
        Worker worker = workerRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Worker account not found for current user"));

        String imagePath = s3Service.generateFileKey(
                currentUser.getUserId(),
                Objects.requireNonNull(portfolioImage.getOriginalFilename())
        );
        s3Service.uploadFile(portfolioImage, imagePath);

        worker.getPortfolioImagePaths().add(imagePath);
        workerRepository.save(worker);

        return new ResponseMessageDto("Portfolio image uploaded successfully");
    }

    @Transactional
    public ResponseMessageDto deletePortfolioImage(String imagePath) {
        var currentUser = permissionService.getLoggedInUser();
        Worker worker = workerRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Worker account not found for current user"));

        if (!worker.getPortfolioImagePaths().contains(imagePath)) {
            throw new ResourceNotFoundException("Portfolio image not found in worker's portfolio");
        }

        worker.getPortfolioImagePaths().remove(imagePath);
        s3Service.deleteFile(imagePath);
        workerRepository.save(worker);

        return new ResponseMessageDto("Portfolio image deleted successfully");
    }
}
