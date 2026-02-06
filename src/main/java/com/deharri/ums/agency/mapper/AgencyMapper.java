package com.deharri.ums.agency.mapper;

import com.deharri.ums.agency.dto.request.CreateAgencyDto;
import com.deharri.ums.agency.dto.request.UpdateAgencyDto;
import com.deharri.ums.agency.dto.response.AgencyListItemDto;
import com.deharri.ums.agency.dto.response.AgencyProfileResponseDto;
import com.deharri.ums.agency.entity.Agency;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        componentModel = "spring"
)
public abstract class AgencyMapper {

    @Autowired
    protected AgencyMapperHelper agencyMapperHelper;

    @Mapping(target = "coreUser", expression = "java(agencyMapperHelper.getCoreUser())")
    @Mapping(target = "agencyId", ignore = true)
    @Mapping(target = "licensePath", ignore = true)
    @Mapping(target = "verificationStatus", ignore = true)
    @Mapping(target = "workers", ignore = true)
    @Mapping(target = "totalWorkers", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalJobsCompleted", ignore = true)
    public abstract Agency createAgencyDtoToAgency(CreateAgencyDto createAgencyDto);

    @Mapping(target = "agencyId", ignore = true)
    @Mapping(target = "coreUser", ignore = true)
    @Mapping(target = "licensePath", ignore = true)
    @Mapping(target = "verificationStatus", ignore = true)
    @Mapping(target = "workers", ignore = true)
    @Mapping(target = "totalWorkers", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalJobsCompleted", ignore = true)
    public abstract void updateAgencyFromDto(UpdateAgencyDto updateDto, @MappingTarget Agency agency);

    @Mapping(target = "agencyId", expression = "java(agency.getAgencyId().toString())")
    @Mapping(target = "userId", expression = "java(agency.getCoreUser().getUserId().toString())")
    @Mapping(target = "username", source = "coreUser.username")
    @Mapping(target = "email", source = "coreUser.userData.email")
    @Mapping(target = "phoneNumber", source = "coreUser.userData.phoneNumber")
    @Mapping(target = "hasLicenseDocument", expression = "java(agency.getLicensePath() != null && !agency.getLicensePath().isBlank())")
    public abstract AgencyProfileResponseDto agencyToProfileResponseDto(Agency agency);

    @Mapping(target = "agencyId", expression = "java(agency.getAgencyId().toString())")
    public abstract AgencyListItemDto agencyToListItemDto(Agency agency);

}
