package com.deharri.ums.worker.mapper;

import com.deharri.ums.permission.PermissionService;
import com.deharri.ums.user.entity.CoreUser;
import lombok.AllArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class WorkerMapperHelper {

    private final PermissionService permissionService;

    @Named("getCoreUser")
    public CoreUser getCoreUser() {
        return permissionService.getLoggedInUser();
    }

}
