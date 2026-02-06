package com.deharri.ums.worker.mapper;

import com.deharri.ums.worker.dto.request.CreateWorkerAccountDto;
import com.deharri.ums.worker.dto.request.UpdateWorkerProfileDto;
import com.deharri.ums.worker.dto.response.WorkerListItemDto;
import com.deharri.ums.worker.dto.response.WorkerProfileResponseDto;
import com.deharri.ums.worker.entity.Worker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        componentModel = "spring"
)
public abstract class WorkerMapper {

    @Autowired
    protected WorkerMapperHelper workerMapperHelper;

    @Mapping(target = "coreUser", expression = "java(workerMapperHelper.getCoreUser())")
    @Mapping(target = "cnicVerification.cnic", source = "cnic")
    @Mapping(target = "agency", expression = "java(workerMapperHelper.getAgency(createWorkerAccountDto.getAgencyId()))")
    @Mapping(target = "workerId", ignore = true)
    @Mapping(target = "availabilityStatus", ignore = true)
    @Mapping(target = "portfolioImagePaths", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalJobsCompleted", ignore = true)
    public abstract Worker createWorkerAccountDtoToWorker(CreateWorkerAccountDto createWorkerAccountDto);

    @Mapping(target = "workerId", ignore = true)
    @Mapping(target = "coreUser", ignore = true)
    @Mapping(target = "availabilityStatus", ignore = true)
    @Mapping(target = "cnicVerification", ignore = true)
    @Mapping(target = "agency", ignore = true)
    @Mapping(target = "portfolioImagePaths", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalJobsCompleted", ignore = true)
    @Mapping(target = "workerType", ignore = true) // Worker type cannot be changed
    public abstract void updateWorkerFromDto(UpdateWorkerProfileDto updateDto, @MappingTarget Worker worker);

    @Mapping(target = "workerId", expression = "java(worker.getWorkerId().toString())")
    @Mapping(target = "userId", expression = "java(worker.getCoreUser().getUserId().toString())")
    @Mapping(target = "username", source = "coreUser.username")
    @Mapping(target = "firstName", source = "coreUser.firstName")
    @Mapping(target = "lastName", source = "coreUser.lastName")
    @Mapping(target = "email", source = "coreUser.userData.email")
    @Mapping(target = "phoneNumber", source = "coreUser.userData.phoneNumber")
    @Mapping(target = "profilePictureUrl", expression = "java(workerMapperHelper.getProfilePictureUrl(worker))")
    @Mapping(target = "portfolioImageUrls", expression = "java(workerMapperHelper.getPortfolioUrls(worker))")
    @Mapping(target = "agency", expression = "java(workerMapperHelper.getAgencyBasicInfo(worker))")
    @Mapping(target = "availabilityStatus", source = "availabilityStatus.availabilityStatus")
    @Mapping(target = "unavailableFrom", source = "availabilityStatus.unavailableFrom")
    @Mapping(target = "unavailableUntil", source = "availabilityStatus.unavailableUntil")
    @Mapping(target = "unavailabilityReason", source = "availabilityStatus.unavailabilityReason")
    @Mapping(target = "isVerified", expression = "java(worker.isVerified())")
    @Mapping(target = "verificationStatus", source = "cnicVerification.verificationStatus")
    public abstract WorkerProfileResponseDto workerToProfileResponseDto(Worker worker);

    @Mapping(target = "workerId", expression = "java(worker.getWorkerId().toString())")
    @Mapping(target = "firstName", source = "coreUser.firstName")
    @Mapping(target = "lastName", source = "coreUser.lastName")
    @Mapping(target = "profilePictureUrl", expression = "java(workerMapperHelper.getProfilePictureUrl(worker))")
    @Mapping(target = "availabilityStatus", source = "availabilityStatus.availabilityStatus")
    @Mapping(target = "isVerified", expression = "java(worker.isVerified())")
    @Mapping(target = "agencyName", expression = "java(worker.getAgency() != null ? worker.getAgency().getAgencyName() : null)")
    public abstract WorkerListItemDto workerToListItemDto(Worker worker);

}