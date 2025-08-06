package com.deharri.ums.user.mapper;

import com.deharri.ums.user.dto.response.UserProfileDto;
import com.deharri.ums.user.entity.CoreUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = UserMapperHelper.class
)
public interface UserMapper {

    @Mapping(target = "email", source = "coreUser.userData.email")
    @Mapping(target = "phoneNumber", source = "coreUser.userData.phoneNumber")
    @Mapping(target = "profilePictureUrl", source = "coreUser.userData.profilePictureUrl")
    UserProfileDto coreUserToUserProfileDto(CoreUser coreUser);

}
