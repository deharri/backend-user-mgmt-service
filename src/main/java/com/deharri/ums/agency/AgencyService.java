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
import com.deharri.ums.enums.UserRole;
import com.deharri.ums.error.exception.AuthorizationException;
import com.deharri.ums.error.exception.CustomDataIntegrityViolationException;
import com.deharri.ums.error.exception.ResourceNotFoundException;
import com.deharri.ums.permission.PermissionService;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
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
public class AgencyService {

    private final AgencyRepository agencyRepository;
    private final AgencyMemberRepository agencyMemberRepository;
    private final AgencyMapper agencyMapper;
    private final PermissionService permissionService;
    private final S3Service s3Service;

    @Transactional
    public ResponseMessageDto createAgency(CreateAgencyDto createAgencyDto) {
        // Check if agency name already exists
        if (agencyRepository.existsByAgencyName(createAgencyDto.getAgencyName())) {
            throw new CustomDataIntegrityViolationException("Agency with this name already exists");
        }

        var currentUser = permissionService.getLoggedInUser();

        // Create agency
        Agency agency = agencyMapper.createAgencyDtoToAgency(createAgencyDto);
        agencyRepository.save(agency);

        // Automatically create agency member entry for the creator with AGENCY_ADMIN role
        AgencyMember adminMember = AgencyMember.builder()
                .agency(agency)
                .coreUser(currentUser)
                .agencyRole(AgencyRole.AGENCY_ADMIN)
                .build();
        agencyMemberRepository.save(adminMember);

        // Add ROLE_AGENCY to the user if not already present
        if (!currentUser.getUserData().getUserRoles().contains(UserRole.ROLE_AGENCY)) {
            currentUser.getUserData().getUserRoles().add(UserRole.ROLE_AGENCY);
        }

        return new ResponseMessageDto("Agency created successfully. You are now the agency admin.");
    }

    public List<AgencyListItemDto> getAllAgencies() {
        return agencyRepository.findAll().stream()
                .map(agencyMapper::agencyToListItemDto)
                .collect(Collectors.toList());
    }

    public AgencyProfileResponseDto getAgencyById(String agencyId) {
        UUID id = UUID.fromString(agencyId);
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found with ID: " + agencyId));
        return agencyMapper.agencyToProfileResponseDto(agency);
    }

    public AgencyProfileResponseDto getMyAgencyProfile() {
        var currentUser = permissionService.getLoggedInUser();
        Agency agency = agencyRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Agency account not found for current user"));
        return agencyMapper.agencyToProfileResponseDto(agency);
    }

    @Transactional
    public ResponseMessageDto updateMyAgencyProfile(UpdateAgencyDto updateDto) {
        var currentUser = permissionService.getLoggedInUser();
        Agency agency = agencyRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Agency account not found for current user"));

        // Check if new agency name conflicts with existing one
        if (updateDto.getAgencyName() != null &&
                !updateDto.getAgencyName().equals(agency.getAgencyName()) &&
                agencyRepository.existsByAgencyName(updateDto.getAgencyName())) {
            throw new CustomDataIntegrityViolationException("Agency with this name already exists");
        }

        agencyMapper.updateAgencyFromDto(updateDto, agency);
        agencyRepository.save(agency);

        return new ResponseMessageDto("Agency profile updated successfully");
    }

    @Transactional
    public ResponseMessageDto uploadLicenseDocument(MultipartFile licenseDocument) {
        var currentUser = permissionService.getLoggedInUser();
        Agency agency = agencyRepository.findByCoreUser(currentUser)
                .orElseThrow(() -> new AuthorizationException("Agency account not found for current user"));

        // Delete old license document if exists
        if (agency.getLicensePath() != null && !agency.getLicensePath().isBlank()) {
            s3Service.deleteFile(agency.getLicensePath());
        }

        String licensePath = s3Service.generateFileKey(
                currentUser.getUserId(),
                Objects.requireNonNull(licenseDocument.getOriginalFilename())
        );
        s3Service.uploadFile(licenseDocument, licensePath);

        agency.setLicensePath(licensePath);
        agencyRepository.save(agency);

        return new ResponseMessageDto("License document uploaded successfully");
    }

}
