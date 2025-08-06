package com.deharri.ums.user;

import com.deharri.ums.annotations.CheckPassword;
import com.deharri.ums.annotations.ValidateArguments;
import com.deharri.ums.permission.PermissionService;
import com.deharri.ums.user.dto.request.UserEmailUpdateDto;
import com.deharri.ums.user.dto.request.UserPasswordUpdateDto;
import com.deharri.ums.user.dto.request.UserPhoneNoUpdateDto;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.user.dto.response.UserProfileDto;
import com.deharri.ums.user.entity.CoreUser;
import com.deharri.ums.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Setter
    private CoreUser currentUser;

    public UserProfileDto getMyProfile() {
        CoreUser coreUser = permissionService.getLoggedInUser();
        return userMapper.coreUserToUserProfileDto(coreUser);
    }

    @CheckPassword
    @ValidateArguments
    @Transactional
    public ResponseMessageDto updatePhoneNo(UserPhoneNoUpdateDto dto) {
        currentUser.getUserData().setPhoneNumber(dto.getNewPhoneNumber());
        userRepository.save(currentUser);
        return new ResponseMessageDto("Phone Number Updated Successfully!");
    }


    @CheckPassword
    @ValidateArguments
    @Transactional
    public ResponseMessageDto updatePassword(UserPasswordUpdateDto dto) {
        currentUser.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(currentUser);
        return new ResponseMessageDto("Password Updated Successfully!");
    }

    @CheckPassword
    @ValidateArguments
    @Transactional
    public ResponseMessageDto updateEmail(UserEmailUpdateDto dto) {
        currentUser.getUserData().setEmail(dto.getNewEmail());
        userRepository.save(currentUser);
        return new ResponseMessageDto("Email Updated Successfully!");
    }

    public boolean isPasswordCorrect(String givenPassword, String actualEncodedPassword) {
        return passwordEncoder.matches(givenPassword, actualEncodedPassword);
    }

    public UserProfileDto getUserProfile(UUID uuid) {
        CoreUser coreUser = userRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("User not found with UUID: " + uuid));
        return userMapper.coreUserToUserProfileDto(coreUser);
    }
}
