package com.deharri.ums.worker.mapper;

import com.deharri.ums.agency.AgencyRepository;
import com.deharri.ums.agency.entity.Agency;
import com.deharri.ums.amazon.S3Service;
import com.deharri.ums.permission.PermissionService;
import com.deharri.ums.user.entity.CoreUser;
import com.deharri.ums.worker.dto.response.WorkerProfileResponseDto;
import com.deharri.ums.worker.entity.Worker;
import lombok.AllArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class WorkerMapperHelper {

    private final PermissionService permissionService;
    private final AgencyRepository agencyRepository;
    private final S3Service s3Service;

    @Named("getCoreUser")
    public CoreUser getCoreUser() {
        return permissionService.getLoggedInUser();
    }

    @Named("getAgency")
    public Agency getAgency(String agencyId) {
        if (agencyId == null || agencyId.isBlank()) {
            return null;
        }
        try {
            UUID id = UUID.fromString(agencyId);
            return agencyRepository.findById(id).orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Named("getProfilePictureUrl")
    public String getProfilePictureUrl(Worker worker) {
        try {
            String profilePicturePath = worker.getCoreUser().getUserData().getProfilePicturePath();
            if (profilePicturePath != null && !profilePicturePath.isBlank()) {
                return s3Service.generatePresignedUrl(profilePicturePath, 100).toString();
            }
        } catch (Exception e) {
            // Return null if there's any issue generating URL
        }
        return null;
    }

    @Named("getPortfolioUrls")
    public List<String> getPortfolioUrls(Worker worker) {
        if (worker.getPortfolioImagePaths() == null || worker.getPortfolioImagePaths().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return worker.getPortfolioImagePaths().stream()
                    .map(path -> {
                        try {
                            return s3Service.generatePresignedUrl(path, 100).toString();
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(url -> url != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Named("getAgencyBasicInfo")
    public WorkerProfileResponseDto.AgencyBasicInfoDto getAgencyBasicInfo(Worker worker) {
        if (worker.getAgency() == null) {
            return null;
        }
        Agency agency = worker.getAgency();
        return WorkerProfileResponseDto.AgencyBasicInfoDto.builder()
                .agencyId(agency.getAgencyId().toString())
                .agencyName(agency.getAgencyName())
                .city(agency.getCity())
                .build();
    }

}
