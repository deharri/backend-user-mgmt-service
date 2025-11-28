package com.deharri.ums.user.mapper;

import com.deharri.ums.amazon.S3Service;
import com.deharri.ums.permission.PermissionService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.net.URL;


@Component
@RequiredArgsConstructor
public class UserMapperHelper {

    private final S3Service s3Service;
    private final PermissionService permissionService;

    @Named("getProfilePictureUrl")
    public URL getProfilePictureUrl(String profilePicturePath) {
        return s3Service.generatePresignedUrl(profilePicturePath, 600);
    }

}
