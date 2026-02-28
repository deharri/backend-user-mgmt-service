package com.deharri.ums.agency;

import com.deharri.ums.agency.dto.request.CreateAgencyDto;
import com.deharri.ums.agency.dto.request.UpdateAgencyDto;
import com.deharri.ums.agency.dto.response.AgencyListItemDto;
import com.deharri.ums.agency.dto.response.AgencyProfileResponseDto;
import com.deharri.ums.agency.entity.Agency;
import com.deharri.ums.agency.entity.AgencyMember;
import com.deharri.ums.agency.mapper.AgencyMapper;
import com.deharri.ums.amazon.S3Service;
import com.deharri.ums.enums.AgencyRole;
import com.deharri.ums.enums.PakistanCity;
import com.deharri.ums.enums.UserRole;
import com.deharri.ums.error.exception.AuthorizationException;
import com.deharri.ums.error.exception.CustomDataIntegrityViolationException;
import com.deharri.ums.error.exception.ResourceNotFoundException;
import com.deharri.ums.permission.PermissionService;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.user.entity.CoreUser;
import com.deharri.ums.user.entity.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgencyService Unit Tests")
class AgencyServiceTest {

    @Mock
    private AgencyRepository agencyRepository;

    @Mock
    private AgencyMemberRepository agencyMemberRepository;

    @Mock
    private AgencyMapper agencyMapper;

    @Mock
    private PermissionService permissionService;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private AgencyService agencyService;

    private CoreUser currentUser;
    private Agency agency;
    private UUID agencyId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        agencyId = UUID.randomUUID();

        currentUser = CoreUser.builder()
                .userId(userId)
                .username("agency_owner")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .userData(UserData.builder()
                        .dataId(UUID.randomUUID())
                        .phoneNumber("+923001234567")
                        .email("john@example.com")
                        .userRoles(new ArrayList<>(List.of(UserRole.ROLE_CONSUMER)))
                        .build())
                .build();

        agency = Agency.builder()
                .agencyId(agencyId)
                .coreUser(currentUser)
                .agencyName("Test Agency")
                .description("A test agency")
                .contactNumber("+923001234567")
                .contactEmail("agency@example.com")
                .city(PakistanCity.LAHORE)
                .address("123 Test Street")
                .serviceCities(new ArrayList<>(List.of(PakistanCity.LAHORE, PakistanCity.KARACHI)))
                .licenseNumber("LIC-001")
                .licensePath(null)
                .verificationStatus(Agency.VerificationStatus.PENDING)
                .totalWorkers(0)
                .averageRating(BigDecimal.ZERO)
                .totalJobsCompleted(0)
                .build();
    }

    // ========================================================================
    // createAgency() tests
    // ========================================================================

    @Nested
    @DisplayName("createAgency()")
    class CreateAgencyTests {

        @Test
        @DisplayName("Should save agency and member when DTO is valid")
        void givenValidDto_whenCreateAgency_thenSaveAgencyAndMember() {
            // given
            CreateAgencyDto createDto = CreateAgencyDto.builder()
                    .agencyName("New Agency")
                    .description("Agency description")
                    .contactNumber("+923001234567")
                    .contactEmail("new@agency.com")
                    .city(PakistanCity.LAHORE)
                    .address("456 Agency Road")
                    .serviceCities(List.of(PakistanCity.LAHORE))
                    .licenseNumber("LIC-002")
                    .build();

            Agency mappedAgency = Agency.builder()
                    .agencyName("New Agency")
                    .coreUser(currentUser)
                    .build();

            when(agencyRepository.existsByAgencyName("New Agency")).thenReturn(false);
            when(permissionService.getLoggedInUser()).thenReturn(currentUser);
            when(agencyMapper.createAgencyDtoToAgency(createDto)).thenReturn(mappedAgency);

            // when
            ResponseMessageDto result = agencyService.createAgency(createDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Agency created successfully. You are now the agency admin.");

            verify(agencyRepository).existsByAgencyName("New Agency");
            verify(permissionService).getLoggedInUser();
            verify(agencyMapper).createAgencyDtoToAgency(createDto);
            verify(agencyRepository).save(mappedAgency);

            ArgumentCaptor<AgencyMember> memberCaptor = ArgumentCaptor.forClass(AgencyMember.class);
            verify(agencyMemberRepository).save(memberCaptor.capture());

            AgencyMember savedMember = memberCaptor.getValue();
            assertThat(savedMember.getAgency()).isEqualTo(mappedAgency);
            assertThat(savedMember.getCoreUser()).isEqualTo(currentUser);
            assertThat(savedMember.getAgencyRole()).isEqualTo(AgencyRole.AGENCY_ADMIN);

            assertThat(currentUser.getUserData().getUserRoles()).contains(UserRole.ROLE_AGENCY);
        }

        @Test
        @DisplayName("Should throw CustomDataIntegrityViolationException when agency name already exists")
        void givenDuplicateName_whenCreateAgency_thenThrowCustomDataIntegrityViolation() {
            // given
            CreateAgencyDto createDto = CreateAgencyDto.builder()
                    .agencyName("Existing Agency")
                    .build();

            when(agencyRepository.existsByAgencyName("Existing Agency")).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> agencyService.createAgency(createDto))
                    .isInstanceOf(CustomDataIntegrityViolationException.class)
                    .hasMessage("Agency with this name already exists");

            verify(agencyRepository).existsByAgencyName("Existing Agency");
            verifyNoInteractions(permissionService);
            verify(agencyRepository, never()).save(any());
            verify(agencyMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should not add duplicate ROLE_AGENCY when user already has it")
        void givenUserAlreadyHasAgencyRole_whenCreateAgency_thenDontAddDuplicateRole() {
            // given
            currentUser.getUserData().getUserRoles().add(UserRole.ROLE_AGENCY);

            CreateAgencyDto createDto = CreateAgencyDto.builder()
                    .agencyName("Another Agency")
                    .build();

            Agency mappedAgency = Agency.builder()
                    .agencyName("Another Agency")
                    .coreUser(currentUser)
                    .build();

            when(agencyRepository.existsByAgencyName("Another Agency")).thenReturn(false);
            when(permissionService.getLoggedInUser()).thenReturn(currentUser);
            when(agencyMapper.createAgencyDtoToAgency(createDto)).thenReturn(mappedAgency);

            // when
            agencyService.createAgency(createDto);

            // then
            long agencyRoleCount = currentUser.getUserData().getUserRoles().stream()
                    .filter(role -> role == UserRole.ROLE_AGENCY)
                    .count();
            assertThat(agencyRoleCount).isEqualTo(1);
        }
    }

    // ========================================================================
    // getAllAgencies() tests
    // ========================================================================

    @Nested
    @DisplayName("getAllAgencies()")
    class GetAllAgenciesTests {

        @Test
        @DisplayName("Should return mapped list when agencies exist")
        void givenAgenciesExist_whenGetAllAgencies_thenReturnMappedList() {
            // given
            Agency agency1 = Agency.builder().agencyId(UUID.randomUUID()).agencyName("Agency 1").build();
            Agency agency2 = Agency.builder().agencyId(UUID.randomUUID()).agencyName("Agency 2").build();

            AgencyListItemDto dto1 = AgencyListItemDto.builder()
                    .agencyId(agency1.getAgencyId().toString())
                    .agencyName("Agency 1")
                    .city(PakistanCity.LAHORE)
                    .verificationStatus(Agency.VerificationStatus.VERIFIED)
                    .totalWorkers(5)
                    .averageRating(new BigDecimal("4.50"))
                    .totalJobsCompleted(10)
                    .build();

            AgencyListItemDto dto2 = AgencyListItemDto.builder()
                    .agencyId(agency2.getAgencyId().toString())
                    .agencyName("Agency 2")
                    .city(PakistanCity.KARACHI)
                    .verificationStatus(Agency.VerificationStatus.PENDING)
                    .totalWorkers(3)
                    .averageRating(new BigDecimal("3.80"))
                    .totalJobsCompleted(7)
                    .build();

            when(agencyRepository.findAll()).thenReturn(List.of(agency1, agency2));
            when(agencyMapper.agencyToListItemDto(agency1)).thenReturn(dto1);
            when(agencyMapper.agencyToListItemDto(agency2)).thenReturn(dto2);

            // when
            List<AgencyListItemDto> result = agencyService.getAllAgencies();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getAgencyName()).isEqualTo("Agency 1");
            assertThat(result.get(1).getAgencyName()).isEqualTo("Agency 2");

            verify(agencyRepository).findAll();
            verify(agencyMapper).agencyToListItemDto(agency1);
            verify(agencyMapper).agencyToListItemDto(agency2);
        }

        @Test
        @DisplayName("Should return empty list when no agencies exist")
        void givenNoAgencies_whenGetAllAgencies_thenReturnEmptyList() {
            // given
            when(agencyRepository.findAll()).thenReturn(Collections.emptyList());

            // when
            List<AgencyListItemDto> result = agencyService.getAllAgencies();

            // then
            assertThat(result).isEmpty();

            verify(agencyRepository).findAll();
        }
    }

    // ========================================================================
    // getAgencyById() tests
    // ========================================================================

    @Nested
    @DisplayName("getAgencyById()")
    class GetAgencyByIdTests {

        @Test
        @DisplayName("Should return profile when agency ID is valid")
        void givenValidId_whenGetAgencyById_thenReturnProfile() {
            // given
            String agencyIdStr = agencyId.toString();

            AgencyProfileResponseDto expectedDto = AgencyProfileResponseDto.builder()
                    .agencyId(agencyIdStr)
                    .agencyName("Test Agency")
                    .city(PakistanCity.LAHORE)
                    .build();

            when(agencyRepository.findById(agencyId)).thenReturn(Optional.of(agency));
            when(agencyMapper.agencyToProfileResponseDto(agency)).thenReturn(expectedDto);

            // when
            AgencyProfileResponseDto result = agencyService.getAgencyById(agencyIdStr);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAgencyId()).isEqualTo(agencyIdStr);
            assertThat(result.getAgencyName()).isEqualTo("Test Agency");

            verify(agencyRepository).findById(agencyId);
            verify(agencyMapper).agencyToProfileResponseDto(agency);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when agency ID is invalid")
        void givenInvalidId_whenGetAgencyById_thenThrowResourceNotFound() {
            // given
            UUID invalidId = UUID.randomUUID();
            String invalidIdStr = invalidId.toString();

            when(agencyRepository.findById(invalidId)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> agencyService.getAgencyById(invalidIdStr))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Agency not found with ID: " + invalidIdStr);

            verify(agencyRepository).findById(invalidId);
        }
    }

    // ========================================================================
    // getMyAgencyProfile() tests
    // ========================================================================

    @Nested
    @DisplayName("getMyAgencyProfile()")
    class GetMyAgencyProfileTests {

        @Test
        @DisplayName("Should return profile when logged-in user owns an agency")
        void givenLoggedInAgencyOwner_whenGetMyAgencyProfile_thenReturnProfile() {
            // given
            AgencyProfileResponseDto expectedDto = AgencyProfileResponseDto.builder()
                    .agencyId(agencyId.toString())
                    .agencyName("Test Agency")
                    .userId(userId.toString())
                    .username("agency_owner")
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(currentUser);
            when(agencyRepository.findByCoreUser(currentUser)).thenReturn(Optional.of(agency));
            when(agencyMapper.agencyToProfileResponseDto(agency)).thenReturn(expectedDto);

            // when
            AgencyProfileResponseDto result = agencyService.getMyAgencyProfile();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAgencyName()).isEqualTo("Test Agency");
            assertThat(result.getUserId()).isEqualTo(userId.toString());

            verify(permissionService).getLoggedInUser();
            verify(agencyRepository).findByCoreUser(currentUser);
            verify(agencyMapper).agencyToProfileResponseDto(agency);
        }

        @Test
        @DisplayName("Should throw AuthorizationException when user has no agency account")
        void givenNoAgencyAccount_whenGetMyAgencyProfile_thenThrowAuthorizationException() {
            // given
            when(permissionService.getLoggedInUser()).thenReturn(currentUser);
            when(agencyRepository.findByCoreUser(currentUser)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> agencyService.getMyAgencyProfile())
                    .isInstanceOf(AuthorizationException.class)
                    .hasMessage("Agency account not found for current user");

            verify(permissionService).getLoggedInUser();
            verify(agencyRepository).findByCoreUser(currentUser);
        }
    }

    // ========================================================================
    // updateMyAgencyProfile() tests
    // ========================================================================

    @Nested
    @DisplayName("updateMyAgencyProfile()")
    class UpdateMyAgencyProfileTests {

        @Test
        @DisplayName("Should update and save when update DTO is valid")
        void givenValidUpdateDto_whenUpdateMyAgencyProfile_thenUpdateAndSave() {
            // given
            UpdateAgencyDto updateDto = UpdateAgencyDto.builder()
                    .agencyName("Updated Agency Name")
                    .description("Updated description")
                    .contactNumber("+923009876543")
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(currentUser);
            when(agencyRepository.findByCoreUser(currentUser)).thenReturn(Optional.of(agency));
            when(agencyRepository.existsByAgencyName("Updated Agency Name")).thenReturn(false);

            // when
            ResponseMessageDto result = agencyService.updateMyAgencyProfile(updateDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Agency profile updated successfully");

            verify(permissionService).getLoggedInUser();
            verify(agencyRepository).findByCoreUser(currentUser);
            verify(agencyRepository).existsByAgencyName("Updated Agency Name");
            verify(agencyMapper).updateAgencyFromDto(updateDto, agency);
            verify(agencyRepository).save(agency);
        }

        @Test
        @DisplayName("Should throw CustomDataIntegrityViolationException when new name is duplicate")
        void givenDuplicateNameInUpdate_whenUpdateMyAgencyProfile_thenThrowCustomDataIntegrityViolation() {
            // given
            UpdateAgencyDto updateDto = UpdateAgencyDto.builder()
                    .agencyName("Taken Agency Name")
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(currentUser);
            when(agencyRepository.findByCoreUser(currentUser)).thenReturn(Optional.of(agency));
            when(agencyRepository.existsByAgencyName("Taken Agency Name")).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> agencyService.updateMyAgencyProfile(updateDto))
                    .isInstanceOf(CustomDataIntegrityViolationException.class)
                    .hasMessage("Agency with this name already exists");

            verify(agencyRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should proceed normally when name in update matches current agency name")
        void givenSameNameInUpdate_whenUpdateMyAgencyProfile_thenProceedNormally() {
            // given
            UpdateAgencyDto updateDto = UpdateAgencyDto.builder()
                    .agencyName("Test Agency") // same as current agency name
                    .description("Updated description only")
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(currentUser);
            when(agencyRepository.findByCoreUser(currentUser)).thenReturn(Optional.of(agency));

            // when
            ResponseMessageDto result = agencyService.updateMyAgencyProfile(updateDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Agency profile updated successfully");

            // existsByAgencyName should NOT be called because the name equals the current name
            verify(agencyRepository, never()).existsByAgencyName(any());
            verify(agencyMapper).updateAgencyFromDto(updateDto, agency);
            verify(agencyRepository).save(agency);
        }

        @Test
        @DisplayName("Should throw AuthorizationException when user has no agency account")
        void givenNoAgencyAccount_whenUpdateMyAgencyProfile_thenThrowAuthorizationException() {
            // given
            UpdateAgencyDto updateDto = UpdateAgencyDto.builder()
                    .agencyName("New Name")
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(currentUser);
            when(agencyRepository.findByCoreUser(currentUser)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> agencyService.updateMyAgencyProfile(updateDto))
                    .isInstanceOf(AuthorizationException.class)
                    .hasMessage("Agency account not found for current user");

            verify(agencyRepository, never()).save(any());
        }
    }

    // ========================================================================
    // uploadLicenseDocument() tests
    // ========================================================================

    @Nested
    @DisplayName("uploadLicenseDocument()")
    class UploadLicenseDocumentTests {

        @Test
        @DisplayName("Should delete old license and upload new when existing license exists")
        void givenLicenseWithExistingOld_whenUploadLicenseDocument_thenDeleteOldAndUploadNew() {
            // given
            agency.setLicensePath("old/license/path.pdf");

            MultipartFile licenseDocument = mock(MultipartFile.class);
            when(licenseDocument.getOriginalFilename()).thenReturn("new-license.pdf");

            String newLicensePath = "deharri/users/" + userId + "/2026/02/24/uuid.pdf";

            when(permissionService.getLoggedInUser()).thenReturn(currentUser);
            when(agencyRepository.findByCoreUser(currentUser)).thenReturn(Optional.of(agency));
            when(s3Service.generateFileKey(eq(userId), eq("new-license.pdf"))).thenReturn(newLicensePath);

            // when
            ResponseMessageDto result = agencyService.uploadLicenseDocument(licenseDocument);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("License document uploaded successfully");

            verify(s3Service).deleteFile("old/license/path.pdf");
            verify(s3Service).generateFileKey(userId, "new-license.pdf");
            verify(s3Service).uploadFile(licenseDocument, newLicensePath);
            assertThat(agency.getLicensePath()).isEqualTo(newLicensePath);
            verify(agencyRepository).save(agency);
        }

        @Test
        @DisplayName("Should upload new license without deleting when no old license exists")
        void givenLicenseWithNoOld_whenUploadLicenseDocument_thenUploadNew() {
            // given
            agency.setLicensePath(null);

            MultipartFile licenseDocument = mock(MultipartFile.class);
            when(licenseDocument.getOriginalFilename()).thenReturn("license.pdf");

            String newLicensePath = "deharri/users/" + userId + "/2026/02/24/uuid.pdf";

            when(permissionService.getLoggedInUser()).thenReturn(currentUser);
            when(agencyRepository.findByCoreUser(currentUser)).thenReturn(Optional.of(agency));
            when(s3Service.generateFileKey(eq(userId), eq("license.pdf"))).thenReturn(newLicensePath);

            // when
            ResponseMessageDto result = agencyService.uploadLicenseDocument(licenseDocument);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("License document uploaded successfully");

            verify(s3Service, never()).deleteFile(any());
            verify(s3Service).generateFileKey(userId, "license.pdf");
            verify(s3Service).uploadFile(licenseDocument, newLicensePath);
            assertThat(agency.getLicensePath()).isEqualTo(newLicensePath);
            verify(agencyRepository).save(agency);
        }

        @Test
        @DisplayName("Should throw AuthorizationException when user has no agency account")
        void givenNoAgencyAccount_whenUploadLicenseDocument_thenThrowAuthorizationException() {
            // given
            MultipartFile licenseDocument = mock(MultipartFile.class);

            when(permissionService.getLoggedInUser()).thenReturn(currentUser);
            when(agencyRepository.findByCoreUser(currentUser)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> agencyService.uploadLicenseDocument(licenseDocument))
                    .isInstanceOf(AuthorizationException.class)
                    .hasMessage("Agency account not found for current user");

            verifyNoInteractions(s3Service);
            verify(agencyRepository, never()).save(any());
        }
    }
}
