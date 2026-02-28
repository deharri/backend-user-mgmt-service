package com.deharri.ums.worker;

import com.deharri.ums.amazon.S3Service;
import com.deharri.ums.enums.Language;
import com.deharri.ums.enums.PakistanCity;
import com.deharri.ums.enums.UserRole;
import com.deharri.ums.error.exception.AuthorizationException;
import com.deharri.ums.error.exception.ResourceNotFoundException;
import com.deharri.ums.permission.PermissionService;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.user.entity.CoreUser;
import com.deharri.ums.user.entity.UserData;
import com.deharri.ums.worker.dto.request.CreateWorkerAccountDto;
import com.deharri.ums.worker.dto.request.UpdateWorkerProfileDto;
import com.deharri.ums.worker.dto.response.WorkerListItemDto;
import com.deharri.ums.worker.dto.response.WorkerProfileResponseDto;
import com.deharri.ums.worker.dto.response.WorkerTypeDto;
import com.deharri.ums.worker.entity.AvailabilityStatus;
import com.deharri.ums.worker.entity.CnicVerification;
import com.deharri.ums.worker.entity.Worker;
import com.deharri.ums.worker.mapper.WorkerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkerService Unit Tests")
class WorkerServiceTest {

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private WorkerMapper workerMapper;

    @Mock
    private PermissionService permissionService;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private WorkerService workerService;

    private CoreUser coreUser;
    private Worker worker;
    private UUID workerId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        workerId = UUID.randomUUID();

        coreUser = CoreUser.builder()
                .userId(userId)
                .username("john_doe")
                .password("encodedPassword123")
                .firstName("John")
                .lastName("Doe")
                .userData(UserData.builder()
                        .dataId(UUID.randomUUID())
                        .phoneNumber("+923001234567")
                        .email("john@example.com")
                        .userRoles(new ArrayList<>(List.of(UserRole.ROLE_CONSUMER)))
                        .build())
                .build();

        worker = Worker.builder()
                .workerId(workerId)
                .coreUser(coreUser)
                .workerType(Worker.WorkerType.ELECTRICIAN)
                .skills(new ArrayList<>(List.of("Wiring", "Circuit Repair")))
                .bio("Experienced electrician")
                .experienceYears(5)
                .hourlyRate(new BigDecimal("500.00"))
                .dailyRate(new BigDecimal("4000.00"))
                .city(PakistanCity.LAHORE)
                .area("Gulberg")
                .serviceCities(new ArrayList<>(List.of(PakistanCity.LAHORE, PakistanCity.ISLAMABAD)))
                .languages(new ArrayList<>(List.of(Language.URDU, Language.ENGLISH)))
                .portfolioImagePaths(new ArrayList<>())
                .averageRating(new BigDecimal("4.50"))
                .totalJobsCompleted(25)
                .availabilityStatus(AvailabilityStatus.builder()
                        .availabilityId(1L)
                        .availabilityStatus(AvailabilityStatus.Status.AVAILABLE)
                        .build())
                .cnicVerification(CnicVerification.builder()
                        .verificationId(1L)
                        .verificationStatus(CnicVerification.Status.PENDING)
                        .cnic("12345-1234567-1")
                        .cnicFrontPath("")
                        .cnicBackPath("")
                        .build())
                .build();
    }

    // ========================================================================
    // createWorkerAccount() tests
    // ========================================================================

    @Nested
    @DisplayName("createWorkerAccount()")
    class CreateWorkerAccountTests {

        @Test
        @DisplayName("Should save worker and add ROLE_WORKER when DTO is valid")
        void givenValidDto_whenCreateWorkerAccount_thenSaveWorkerAndAddRole() {
            // given
            CreateWorkerAccountDto dto = CreateWorkerAccountDto.builder()
                    .workerType(Worker.WorkerType.ELECTRICIAN)
                    .skills(List.of("Wiring", "Circuit Repair"))
                    .cnic("12345-1234567-1")
                    .bio("Experienced electrician")
                    .experienceYears(5)
                    .hourlyRate(new BigDecimal("500.00"))
                    .dailyRate(new BigDecimal("4000.00"))
                    .city(PakistanCity.LAHORE)
                    .area("Gulberg")
                    .serviceCities(List.of(PakistanCity.LAHORE))
                    .languages(List.of(Language.URDU))
                    .build();

            Worker mappedWorker = Worker.builder()
                    .coreUser(coreUser)
                    .workerType(Worker.WorkerType.ELECTRICIAN)
                    .skills(new ArrayList<>(List.of("Wiring", "Circuit Repair")))
                    .build();

            when(workerMapper.createWorkerAccountDtoToWorker(dto)).thenReturn(mappedWorker);
            when(workerRepository.save(mappedWorker)).thenReturn(mappedWorker);

            // when
            ResponseMessageDto result = workerService.createWorkerAccount(dto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Worker account created successfully");
            assertThat(mappedWorker.getCoreUser().getUserData().getUserRoles()).contains(UserRole.ROLE_WORKER);

            verify(workerMapper).createWorkerAccountDtoToWorker(dto);
            verify(workerRepository).save(mappedWorker);
        }
    }

    // ========================================================================
    // getAllWorkerTypes() tests
    // ========================================================================

    @Nested
    @DisplayName("getAllWorkerTypes()")
    class GetAllWorkerTypesTests {

        @Test
        @DisplayName("Should return all worker types")
        void whenGetAllWorkerTypes_thenReturnAllTypes() {
            // when
            List<WorkerTypeDto> result = workerService.getAllWorkerTypes();

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(Worker.WorkerType.values().length);
            assertThat(result).extracting(WorkerTypeDto::getEnumValue)
                    .contains("MECHANIC", "ELECTRICIAN", "PLUMBER", "CARPENTER",
                            "WELDER", "PAINTER", "MASON", "HVAC_TECHNICIAN", "GENERAL_LABORER");
        }
    }

    // ========================================================================
    // submitCnicForVerification() tests
    // ========================================================================

    @Nested
    @DisplayName("submitCnicForVerification()")
    class SubmitCnicForVerificationTests {

        @Test
        @DisplayName("Should upload CNIC files and save paths when worker exists")
        void givenLoggedInWorker_whenSubmitCnicForVerification_thenUploadAndSave() {
            // given
            MultipartFile cnicFront = mock(MultipartFile.class);
            MultipartFile cnicBack = mock(MultipartFile.class);

            when(cnicFront.getOriginalFilename()).thenReturn("cnic_front.jpg");
            when(cnicBack.getOriginalFilename()).thenReturn("cnic_back.jpg");

            String frontPath = "deharri/users/" + userId + "/2026/02/24/front-uuid.jpg";
            String backPath = "deharri/users/" + userId + "/2026/02/24/back-uuid.jpg";

            when(permissionService.getLoggedInUser()).thenReturn(coreUser);
            when(s3Service.generateFileKey(eq(userId), eq("cnic_front.jpg"))).thenReturn(frontPath);
            when(s3Service.generateFileKey(eq(userId), eq("cnic_back.jpg"))).thenReturn(backPath);
            when(workerRepository.findByCoreUser(coreUser)).thenReturn(Optional.of(worker));
            when(workerRepository.save(worker)).thenReturn(worker);

            // when
            String result = workerService.submitCnicForVerification(cnicFront, cnicBack);

            // then
            assertThat(result).isEqualTo("CNIC submitted for verification successfully");
            assertThat(worker.getCnicVerification().getCnicFrontPath()).isEqualTo(frontPath);
            assertThat(worker.getCnicVerification().getCnicBackPath()).isEqualTo(backPath);

            verify(s3Service).uploadFile(cnicFront, frontPath);
            verify(s3Service).uploadFile(cnicBack, backPath);
            verify(workerRepository).save(worker);
        }

        @Test
        @DisplayName("Should throw AuthorizationException when worker account not found")
        void givenNoWorkerAccount_whenSubmitCnicForVerification_thenThrowAuthorizationException() {
            // given
            MultipartFile cnicFront = mock(MultipartFile.class);
            MultipartFile cnicBack = mock(MultipartFile.class);

            when(cnicFront.getOriginalFilename()).thenReturn("cnic_front.jpg");
            when(cnicBack.getOriginalFilename()).thenReturn("cnic_back.jpg");

            when(permissionService.getLoggedInUser()).thenReturn(coreUser);
            when(s3Service.generateFileKey(eq(userId), eq("cnic_front.jpg"))).thenReturn("front-path");
            when(s3Service.generateFileKey(eq(userId), eq("cnic_back.jpg"))).thenReturn("back-path");
            when(workerRepository.findByCoreUser(coreUser)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> workerService.submitCnicForVerification(cnicFront, cnicBack))
                    .isInstanceOf(AuthorizationException.class)
                    .hasMessage("Worker account not found for given user");
        }
    }

    // ========================================================================
    // getAllWorkers() tests
    // ========================================================================

    @Nested
    @DisplayName("getAllWorkers()")
    class GetAllWorkersTests {

        @Test
        @DisplayName("Should return mapped list when workers exist")
        void givenWorkersExist_whenGetAllWorkers_thenReturnMappedList() {
            // given
            Worker worker2 = Worker.builder()
                    .workerId(UUID.randomUUID())
                    .coreUser(CoreUser.builder()
                            .userId(UUID.randomUUID())
                            .username("jane_doe")
                            .firstName("Jane")
                            .lastName("Doe")
                            .password("encoded")
                            .userData(UserData.builder()
                                    .dataId(UUID.randomUUID())
                                    .phoneNumber("+923009876543")
                                    .build())
                            .build())
                    .workerType(Worker.WorkerType.PLUMBER)
                    .build();

            WorkerListItemDto listItem1 = WorkerListItemDto.builder()
                    .workerId(workerId.toString())
                    .firstName("John")
                    .lastName("Doe")
                    .workerType(Worker.WorkerType.ELECTRICIAN)
                    .build();

            WorkerListItemDto listItem2 = WorkerListItemDto.builder()
                    .workerId(worker2.getWorkerId().toString())
                    .firstName("Jane")
                    .lastName("Doe")
                    .workerType(Worker.WorkerType.PLUMBER)
                    .build();

            when(workerRepository.findAll()).thenReturn(List.of(worker, worker2));
            when(workerMapper.workerToListItemDto(worker)).thenReturn(listItem1);
            when(workerMapper.workerToListItemDto(worker2)).thenReturn(listItem2);

            // when
            List<WorkerListItemDto> result = workerService.getAllWorkers();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getFirstName()).isEqualTo("John");
            assertThat(result.get(1).getFirstName()).isEqualTo("Jane");

            verify(workerRepository).findAll();
            verify(workerMapper, times(2)).workerToListItemDto(any(Worker.class));
        }

        @Test
        @DisplayName("Should return empty list when no workers exist")
        void givenNoWorkers_whenGetAllWorkers_thenReturnEmptyList() {
            // given
            when(workerRepository.findAll()).thenReturn(List.of());

            // when
            List<WorkerListItemDto> result = workerService.getAllWorkers();

            // then
            assertThat(result).isEmpty();

            verify(workerRepository).findAll();
            verify(workerMapper, never()).workerToListItemDto(any(Worker.class));
        }
    }

    // ========================================================================
    // getWorkerById() tests
    // ========================================================================

    @Nested
    @DisplayName("getWorkerById()")
    class GetWorkerByIdTests {

        @Test
        @DisplayName("Should return worker profile when worker exists")
        void givenValidId_whenGetWorkerById_thenReturnWorkerProfile() {
            // given
            String workerIdStr = workerId.toString();

            WorkerProfileResponseDto profileDto = WorkerProfileResponseDto.builder()
                    .workerId(workerIdStr)
                    .firstName("John")
                    .lastName("Doe")
                    .workerType(Worker.WorkerType.ELECTRICIAN)
                    .experienceYears(5)
                    .build();

            when(workerRepository.findById(workerId)).thenReturn(Optional.of(worker));
            when(workerMapper.workerToProfileResponseDto(worker)).thenReturn(profileDto);

            // when
            WorkerProfileResponseDto result = workerService.getWorkerById(workerIdStr);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getWorkerId()).isEqualTo(workerIdStr);
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getWorkerType()).isEqualTo(Worker.WorkerType.ELECTRICIAN);

            verify(workerRepository).findById(workerId);
            verify(workerMapper).workerToProfileResponseDto(worker);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when worker does not exist")
        void givenInvalidId_whenGetWorkerById_thenThrowResourceNotFoundException() {
            // given
            UUID invalidId = UUID.randomUUID();
            String invalidIdStr = invalidId.toString();

            when(workerRepository.findById(invalidId)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> workerService.getWorkerById(invalidIdStr))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Worker not found with ID: " + invalidIdStr);

            verify(workerRepository).findById(invalidId);
            verify(workerMapper, never()).workerToProfileResponseDto(any(Worker.class));
        }
    }

    // ========================================================================
    // getMyWorkerProfile() tests
    // ========================================================================

    @Nested
    @DisplayName("getMyWorkerProfile()")
    class GetMyWorkerProfileTests {

        @Test
        @DisplayName("Should return profile when logged-in user has a worker account")
        void givenLoggedInWorker_whenGetMyWorkerProfile_thenReturnProfile() {
            // given
            WorkerProfileResponseDto profileDto = WorkerProfileResponseDto.builder()
                    .workerId(workerId.toString())
                    .userId(userId.toString())
                    .firstName("John")
                    .lastName("Doe")
                    .workerType(Worker.WorkerType.ELECTRICIAN)
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(coreUser);
            when(workerRepository.findByCoreUser(coreUser)).thenReturn(Optional.of(worker));
            when(workerMapper.workerToProfileResponseDto(worker)).thenReturn(profileDto);

            // when
            WorkerProfileResponseDto result = workerService.getMyWorkerProfile();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getWorkerId()).isEqualTo(workerId.toString());
            assertThat(result.getFirstName()).isEqualTo("John");

            verify(permissionService).getLoggedInUser();
            verify(workerRepository).findByCoreUser(coreUser);
            verify(workerMapper).workerToProfileResponseDto(worker);
        }

        @Test
        @DisplayName("Should throw AuthorizationException when no worker account exists for current user")
        void givenNoWorkerAccount_whenGetMyWorkerProfile_thenThrowAuthorizationException() {
            // given
            when(permissionService.getLoggedInUser()).thenReturn(coreUser);
            when(workerRepository.findByCoreUser(coreUser)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> workerService.getMyWorkerProfile())
                    .isInstanceOf(AuthorizationException.class)
                    .hasMessage("Worker account not found for current user");

            verify(permissionService).getLoggedInUser();
            verify(workerRepository).findByCoreUser(coreUser);
            verify(workerMapper, never()).workerToProfileResponseDto(any(Worker.class));
        }
    }

    // ========================================================================
    // updateMyWorkerProfile() tests
    // ========================================================================

    @Nested
    @DisplayName("updateMyWorkerProfile()")
    class UpdateMyWorkerProfileTests {

        @Test
        @DisplayName("Should update and save worker when update DTO is valid")
        void givenValidUpdateDto_whenUpdateMyWorkerProfile_thenUpdateAndSave() {
            // given
            UpdateWorkerProfileDto updateDto = UpdateWorkerProfileDto.builder()
                    .skills(List.of("Wiring", "Circuit Repair", "Solar Panel Installation"))
                    .bio("Updated bio with more experience")
                    .experienceYears(7)
                    .hourlyRate(new BigDecimal("600.00"))
                    .dailyRate(new BigDecimal("4500.00"))
                    .city(PakistanCity.ISLAMABAD)
                    .area("F-8")
                    .serviceCities(List.of(PakistanCity.ISLAMABAD, PakistanCity.RAWALPINDI))
                    .languages(List.of(Language.URDU, Language.ENGLISH, Language.PUNJABI))
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(coreUser);
            when(workerRepository.findByCoreUser(coreUser)).thenReturn(Optional.of(worker));
            when(workerRepository.save(worker)).thenReturn(worker);

            // when
            ResponseMessageDto result = workerService.updateMyWorkerProfile(updateDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Worker profile updated successfully");

            verify(permissionService).getLoggedInUser();
            verify(workerRepository).findByCoreUser(coreUser);
            verify(workerMapper).updateWorkerFromDto(updateDto, worker);
            verify(workerRepository).save(worker);
        }

        @Test
        @DisplayName("Should throw AuthorizationException when no worker account exists for current user")
        void givenNoWorkerAccount_whenUpdateMyWorkerProfile_thenThrowAuthorizationException() {
            // given
            UpdateWorkerProfileDto updateDto = UpdateWorkerProfileDto.builder()
                    .bio("Updated bio")
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(coreUser);
            when(workerRepository.findByCoreUser(coreUser)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> workerService.updateMyWorkerProfile(updateDto))
                    .isInstanceOf(AuthorizationException.class)
                    .hasMessage("Worker account not found for current user");

            verify(permissionService).getLoggedInUser();
            verify(workerRepository).findByCoreUser(coreUser);
            verify(workerMapper, never()).updateWorkerFromDto(any(), any());
            verify(workerRepository, never()).save(any());
        }
    }

    // ========================================================================
    // uploadPortfolioImage() tests
    // ========================================================================

    @Nested
    @DisplayName("uploadPortfolioImage()")
    class UploadPortfolioImageTests {

        @Test
        @DisplayName("Should upload image and add path to worker portfolio")
        void givenValidImage_whenUploadPortfolioImage_thenUploadAndAddPath() {
            // given
            MultipartFile portfolioImage = mock(MultipartFile.class);
            when(portfolioImage.getOriginalFilename()).thenReturn("portfolio_1.jpg");

            String imagePath = "deharri/users/" + userId + "/2026/02/24/img-uuid.jpg";

            when(permissionService.getLoggedInUser()).thenReturn(coreUser);
            when(workerRepository.findByCoreUser(coreUser)).thenReturn(Optional.of(worker));
            when(s3Service.generateFileKey(eq(userId), eq("portfolio_1.jpg"))).thenReturn(imagePath);
            when(workerRepository.save(worker)).thenReturn(worker);

            // when
            ResponseMessageDto result = workerService.uploadPortfolioImage(portfolioImage);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Portfolio image uploaded successfully");
            assertThat(worker.getPortfolioImagePaths()).contains(imagePath);

            verify(s3Service).generateFileKey(userId, "portfolio_1.jpg");
            verify(s3Service).uploadFile(portfolioImage, imagePath);
            verify(workerRepository).save(worker);
        }

        @Test
        @DisplayName("Should throw AuthorizationException when no worker account exists for current user")
        void givenNoWorkerAccount_whenUploadPortfolioImage_thenThrowAuthorizationException() {
            // given
            MultipartFile portfolioImage = mock(MultipartFile.class);

            when(permissionService.getLoggedInUser()).thenReturn(coreUser);
            when(workerRepository.findByCoreUser(coreUser)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> workerService.uploadPortfolioImage(portfolioImage))
                    .isInstanceOf(AuthorizationException.class)
                    .hasMessage("Worker account not found for current user");

            verify(s3Service, never()).uploadFile(any(), any());
            verify(workerRepository, never()).save(any());
        }
    }

    // ========================================================================
    // deletePortfolioImage() tests
    // ========================================================================

    @Nested
    @DisplayName("deletePortfolioImage()")
    class DeletePortfolioImageTests {

        @Test
        @DisplayName("Should remove path and delete file when image exists in portfolio")
        void givenExistingImagePath_whenDeletePortfolioImage_thenRemoveAndDelete() {
            // given
            String imagePath = "deharri/users/" + userId + "/2026/02/24/existing-image.jpg";
            worker.getPortfolioImagePaths().add(imagePath);

            when(permissionService.getLoggedInUser()).thenReturn(coreUser);
            when(workerRepository.findByCoreUser(coreUser)).thenReturn(Optional.of(worker));
            when(workerRepository.save(worker)).thenReturn(worker);

            // when
            ResponseMessageDto result = workerService.deletePortfolioImage(imagePath);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Portfolio image deleted successfully");
            assertThat(worker.getPortfolioImagePaths()).doesNotContain(imagePath);

            verify(s3Service).deleteFile(imagePath);
            verify(workerRepository).save(worker);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when image path is not in portfolio")
        void givenNonExistingImagePath_whenDeletePortfolioImage_thenThrowResourceNotFoundException() {
            // given
            String nonExistingPath = "deharri/users/" + userId + "/2026/02/24/non-existing.jpg";

            when(permissionService.getLoggedInUser()).thenReturn(coreUser);
            when(workerRepository.findByCoreUser(coreUser)).thenReturn(Optional.of(worker));

            // when / then
            assertThatThrownBy(() -> workerService.deletePortfolioImage(nonExistingPath))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Portfolio image not found in worker's portfolio");

            verify(s3Service, never()).deleteFile(any());
            verify(workerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw AuthorizationException when no worker account exists for current user")
        void givenNoWorkerAccount_whenDeletePortfolioImage_thenThrowAuthorizationException() {
            // given
            String imagePath = "deharri/users/" + userId + "/2026/02/24/some-image.jpg";

            when(permissionService.getLoggedInUser()).thenReturn(coreUser);
            when(workerRepository.findByCoreUser(coreUser)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> workerService.deletePortfolioImage(imagePath))
                    .isInstanceOf(AuthorizationException.class)
                    .hasMessage("Worker account not found for current user");

            verify(s3Service, never()).deleteFile(any());
            verify(workerRepository, never()).save(any());
        }
    }
}
