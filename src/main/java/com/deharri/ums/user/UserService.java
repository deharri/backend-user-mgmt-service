package com.deharri.ums.user;

import com.deharri.ums.permission.PermissionService;
import com.deharri.ums.user.dto.UserProfileDto;
import com.deharri.ums.user.entity.CoreUser;
import com.deharri.ums.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PermissionService permissionService;

    private final UserMapper userMapper;


    public UserProfileDto getMyProfile() {
        CoreUser coreUser = permissionService.getLoggedInUser();
        return userMapper.coreUserToUserProfileDto(coreUser);
    }
}
