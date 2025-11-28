package com.deharri.ums.user.mapper;

import com.deharri.ums.user.dto.response.UserProfileDto;
import com.deharri.ums.user.entity.CoreUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        componentModel = "spring"
)
public abstract class UserMapper {

    @Autowired
    protected UserMapperHelper userMapperHelper;

    @Mapping(target = "email", source = "coreUser.userData.email")
    @Mapping(target = "phoneNumber", source = "coreUser.userData.phoneNumber")
    @Mapping(target = "profilePictureUrl", expression = "java(userMapperHelper.getProfilePictureUrl(coreUser.getUserData().getProfilePicturePath()))")
    public abstract UserProfileDto coreUserToUserProfileDto(CoreUser coreUser);

}
