package com.deharri.ums.user;

import com.deharri.ums.amazon.S3Service;
import com.deharri.ums.amazon.dto.SignedUrlDto;
import com.deharri.ums.annotations.CheckPassword;
import com.deharri.ums.annotations.ValidateArguments;
import com.deharri.ums.enums.UserRole;
import com.deharri.ums.permission.PermissionService;
import com.deharri.ums.user.dto.request.UserEmailUpdateDto;
import com.deharri.ums.user.dto.request.UserPasswordUpdateDto;
import com.deharri.ums.user.dto.request.UserPhoneNoUpdateDto;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.user.dto.response.UserProfileDto;
import com.deharri.ums.user.entity.CoreUser;
import com.deharri.ums.user.mapper.UserMapper;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    public UserProfileDto getMyProfile() {
        return userMapper.coreUserToUserProfileDto(permissionService.getLoggedInUser());
    }

    @CheckPassword
    @ValidateArguments
    @Transactional
    public ResponseMessageDto updatePhoneNo(UserPhoneNoUpdateDto dto) {
        CoreUser currentUser = permissionService.getLoggedInUser();
        currentUser.getUserData().setPhoneNumber(dto.getNewPhoneNumber());
        userRepository.save(currentUser);
        return new ResponseMessageDto("Phone Number Updated Successfully!");
    }


    @CheckPassword
    @ValidateArguments
    @Transactional
    public ResponseMessageDto updatePassword(UserPasswordUpdateDto dto) {
        CoreUser currentUser = permissionService.getLoggedInUser();
        currentUser.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(currentUser);
        return new ResponseMessageDto("Password Updated Successfully!");
    }

    @CheckPassword
    @ValidateArguments
    @Transactional
    public ResponseMessageDto updateEmail(UserEmailUpdateDto dto) {
        CoreUser currentUser = permissionService.getLoggedInUser();
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

    public List<UserProfileDto> getAllUserProfiles() {
        List<CoreUser> coreUsers = userRepository.findAll();
        return coreUsers.stream()
                .map(userMapper::coreUserToUserProfileDto)
                .toList();
    }

    @Transactional
    public ResponseMessageDto updateProfilePicture(@NotNull MultipartFile picture) {
        CoreUser currentUser = permissionService.getLoggedInUser();
        String oldPictureUrl = currentUser.getUserData().getProfilePicturePath();
        if (oldPictureUrl != null && !oldPictureUrl.isEmpty()) {
            s3Service.deleteFile(oldPictureUrl);
        }
        String picturePath = s3Service.generateFileKey(currentUser.getUserId(), Objects.requireNonNull(picture.getOriginalFilename()));
        s3Service.uploadFile(picture, picturePath);
        currentUser.getUserData().setProfilePicturePath(picturePath);
        userRepository.save(currentUser);
        return new ResponseMessageDto("Profile Picture Updated Successfully!");
    }

    public SignedUrlDto getMyProfilePictureUrl() {
        URL url = s3Service.generatePresignedUrl(permissionService.getLoggedInUser().getUserData().getProfilePicturePath(), 100);
        return new SignedUrlDto(url);
    }

    public List<UserRole> getUserRoles(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username))
                .getUserData().getUserRoles();
    }
}
