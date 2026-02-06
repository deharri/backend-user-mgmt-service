package com.deharri.ums.agency.mapper;

import com.deharri.ums.permission.PermissionService;
import com.deharri.ums.user.entity.CoreUser;
import lombok.AllArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AgencyMapperHelper {

    private final PermissionService permissionService;

    @Named("getCoreUser")
    public CoreUser getCoreUser() {
        return permissionService.getLoggedInUser();
    }

}
