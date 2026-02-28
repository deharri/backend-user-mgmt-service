package com.deharri.ums.user;

import com.deharri.ums.amazon.S3Service;
import com.deharri.ums.amazon.dto.SignedUrlDto;
import com.deharri.ums.enums.UserRole;
import com.deharri.ums.permission.PermissionService;
import com.deharri.ums.user.dto.request.UserEmailUpdateDto;
import com.deharri.ums.user.dto.request.UserPasswordUpdateDto;
import com.deharri.ums.user.dto.request.UserPhoneNoUpdateDto;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.user.dto.response.UserProfileDto;
import com.deharri.ums.user.entity.CoreUser;
import com.deharri.ums.user.entity.UserData;
import com.deharri.ums.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionService permissionService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private UserService userService;

    private CoreUser coreUser;
    private UserData userData;
    private UserProfileDto userProfileDto;
    private UUID userId;

    @BeforeEach
    void setUp() throws MalformedURLException {
        userId = UUID.randomUUID();

        userData = UserData.builder()
                .dataId(UUID.randomUUID())
                .phoneNumber("+1234567890")
                .email("john@example.com")
                .profilePicturePath("deharri/users/pic.jpg")
                .userRoles(List.of(UserRole.ROLE_CONSUMER, UserRole.ROLE_WORKER))
                .build();

        coreUser = CoreUser.builder()
                .userId(userId)
                .username("john_doe")
                .password("encodedPassword123")
                .firstName("John")
                .lastName("Doe")
                .userData(userData)
                .build();

        userProfileDto = UserProfileDto.builder()
                .userId(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("+1234567890")
                .profilePictureUrl(new URL("https://bucket.s3.amazonaws.com/pic.jpg"))
                .build();
    }

    // ========================================================================
    // getMyProfile() tests
    // ========================================================================

    @Nested
    @DisplayName("getMyProfile()")
    class GetMyProfileTests {

        @Test
        @DisplayName("Should return user profile for the logged-in user")
        void givenLoggedInUser_whenGetMyProfile_thenReturnUserProfile() {
            // given
            when(permissionService.getLoggedInUser()).thenReturn(coreUser);
            when(userMapper.coreUserToUserProfileDto(coreUser)).thenReturn(userProfileDto);

            // when
            UserProfileDto result = userService.getMyProfile();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.getEmail()).isEqualTo("john@example.com");
            assertThat(result.getPhoneNumber()).isEqualTo("+1234567890");

            verify(permissionService).getLoggedInUser();
            verify(userMapper).coreUserToUserProfileDto(coreUser);
        }
    }

    // ========================================================================
    // getUserProfile(UUID) tests
    // ========================================================================

    @Nested
    @DisplayName("getUserProfile(UUID)")
    class GetUserProfileTests {

        @Test
        @DisplayName("Should return user profile when UUID exists")
        void givenValidUuid_whenGetUserProfile_thenReturnUserProfile() {
            // given
            when(userRepository.findById(userId)).thenReturn(Optional.of(coreUser));
            when(userMapper.coreUserToUserProfileDto(coreUser)).thenReturn(userProfileDto);

            // when
            UserProfileDto result = userService.getUserProfile(userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");

            verify(userRepository).findById(userId);
            verify(userMapper).coreUserToUserProfileDto(coreUser);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when UUID does not exist")
        void givenInvalidUuid_whenGetUserProfile_thenThrowIllegalArgument() {
            // given
            UUID invalidId = UUID.randomUUID();
            when(userRepository.findById(invalidId)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> userService.getUserProfile(invalidId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User not found with UUID: " + invalidId);

            verify(userRepository).findById(invalidId);
            verify(userMapper, never()).coreUserToUserProfileDto(any());
        }
    }

    // ========================================================================
    // getAllUserProfiles() tests
    // ========================================================================

    @Nested
    @DisplayName("getAllUserProfiles()")
    class GetAllUserProfilesTests {

        @Test
        @DisplayName("Should return list of profiles when users exist")
        void givenUsersExist_whenGetAllUserProfiles_thenReturnList() throws MalformedURLException {
            // given
            CoreUser secondUser = CoreUser.builder()
                    .userId(UUID.randomUUID())
                    .username("jane_doe")
                    .password("encodedPass456")
                    .firstName("Jane")
                    .lastName("Doe")
                    .userData(UserData.builder()
                            .dataId(UUID.randomUUID())
                            .phoneNumber("+9876543210")
                            .email("jane@example.com")
                            .build())
                    .build();

            UserProfileDto secondProfileDto = UserProfileDto.builder()
                    .userId(secondUser.getUserId())
                    .firstName("Jane")
                    .lastName("Doe")
                    .email("jane@example.com")
                    .phoneNumber("+9876543210")
                    .build();

            when(userRepository.findAll()).thenReturn(List.of(coreUser, secondUser));
            when(userMapper.coreUserToUserProfileDto(coreUser)).thenReturn(userProfileDto);
            when(userMapper.coreUserToUserProfileDto(secondUser)).thenReturn(secondProfileDto);

            // when
            List<UserProfileDto> result = userService.getAllUserProfiles();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getFirstName()).isEqualTo("John");
            assertThat(result.get(1).getFirstName()).isEqualTo("Jane");

            verify(userRepository).findAll();
            verify(userMapper).coreUserToUserProfileDto(coreUser);
            verify(userMapper).coreUserToUserProfileDto(secondUser);
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void givenNoUsers_whenGetAllUserProfiles_thenReturnEmptyList() {
            // given
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            // when
            List<UserProfileDto> result = userService.getAllUserProfiles();

            // then
            assertThat(result).isEmpty();

            verify(userRepository).findAll();
        }
    }

    // ========================================================================
    // updatePassword() tests
    // ========================================================================

    @Nested
    @DisplayName("updatePassword()")
    class UpdatePasswordTests {

        @Test
        @DisplayName("Should encode and save new password and return success message")
        void givenValidDto_whenUpdatePassword_thenReturnSuccessMessage() {
            // given
            UserPasswordUpdateDto dto = UserPasswordUpdateDto.builder()
                    .oldPassword("OldP@ss123")
                    .newPassword("NewSecureP@ss456")
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(coreUser);
            when(passwordEncoder.encode("NewSecureP@ss456")).thenReturn("encodedNewPassword");
            when(userRepository.save(coreUser)).thenReturn(coreUser);

            // when
            ResponseMessageDto result = userService.updatePassword(dto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Password Updated Successfully!");
            assertThat(coreUser.getPassword()).isEqualTo("encodedNewPassword");

            verify(permissionService).getLoggedInUser();
            verify(passwordEncoder).encode("NewSecureP@ss456");
            verify(userRepository).save(coreUser);
        }
    }

    // ========================================================================
    // updateEmail() tests
    // ========================================================================

    @Nested
    @DisplayName("updateEmail()")
    class UpdateEmailTests {

        @Test
        @DisplayName("Should update email on user data and return success message")
        void givenValidDto_whenUpdateEmail_thenReturnSuccessMessage() {
            // given
            UserEmailUpdateDto dto = UserEmailUpdateDto.builder()
                    .oldPassword("OldP@ss123")
                    .newEmail("newemail@example.com")
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(coreUser);
            when(userRepository.save(coreUser)).thenReturn(coreUser);

            // when
            ResponseMessageDto result = userService.updateEmail(dto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Email Updated Successfully!");
            assertThat(coreUser.getUserData().getEmail()).isEqualTo("newemail@example.com");

            verify(permissionService).getLoggedInUser();
            verify(userRepository).save(coreUser);
        }
    }

    // ========================================================================
    // updatePhoneNo() tests
    // ========================================================================

    @Nested
    @DisplayName("updatePhoneNo()")
    class UpdatePhoneNoTests {

        @Test
        @DisplayName("Should update phone number on user data and return success message")
        void givenValidDto_whenUpdatePhoneNo_thenReturnSuccessMessage() {
            // given
            UserPhoneNoUpdateDto dto = UserPhoneNoUpdateDto.builder()
                    .oldPassword("OldP@ss123")
                    .newPhoneNumber("+9876543210")
                    .build();

            when(permissionService.getLoggedInUser()).thenReturn(coreUser);
            when(userRepository.save(coreUser)).thenReturn(coreUser);

            // when
            ResponseMessageDto result = userService.updatePhoneNo(dto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Phone Number Updated Successfully!");
            assertThat(coreUser.getUserData().getPhoneNumber()).isEqualTo("+9876543210");

            verify(permissionService).getLoggedInUser();
            verify(userRepository).save(coreUser);
        }
    }

    // ========================================================================
    // updateProfilePicture() tests
    // ========================================================================

    @Nested
    @DisplayName("updateProfilePicture()")
    class UpdateProfilePictureTests {

        @Test
        @DisplayName("Should delete old picture and upload new one when old picture exists")
        void givenPictureWithExistingOldPicture_whenUpdateProfilePicture_thenDeleteOldAndUploadNew() {
            // given
            coreUser.getUserData().setProfilePicturePath("deharri/users/old-pic.jpg");

            MultipartFile picture = mock(MultipartFile.class);
            when(picture.getOriginalFilename()).thenReturn("new-avatar.png");

            String generatedKey = "deharri/users/" + userId + "/2026/02/24/uuid.png";

            when(permissionService.getLoggedInUser()).thenReturn(coreUser);
            when(s3Service.generateFileKey(eq(userId), eq("new-avatar.png"))).thenReturn(generatedKey);
            when(userRepository.save(coreUser)).thenReturn(coreUser);

            // when
            ResponseMessageDto result = userService.updateProfilePicture(picture);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Profile Picture Updated Successfully!");
            assertThat(coreUser.getUserData().getProfilePicturePath()).isEqualTo(generatedKey);

            verify(s3Service).deleteFile("deharri/users/old-pic.jpg");
            verify(s3Service).generateFileKey(userId, "new-avatar.png");
            verify(s3Service).uploadFile(picture, generatedKey);
            verify(userRepository).save(coreUser);
        }

        @Test
        @DisplayName("Should upload new picture without deleting when no old picture exists")
        void givenPictureWithNoOldPicture_whenUpdateProfilePicture_thenUploadNew() {
            // given
            coreUser.getUserData().setProfilePicturePath(null);

            MultipartFile picture = mock(MultipartFile.class);
            when(picture.getOriginalFilename()).thenReturn("avatar.jpg");

            String generatedKey = "deharri/users/" + userId + "/2026/02/24/uuid.jpg";

            when(permissionService.getLoggedInUser()).thenReturn(coreUser);
            when(s3Service.generateFileKey(eq(userId), eq("avatar.jpg"))).thenReturn(generatedKey);
            when(userRepository.save(coreUser)).thenReturn(coreUser);

            // when
            ResponseMessageDto result = userService.updateProfilePicture(picture);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Profile Picture Updated Successfully!");
            assertThat(coreUser.getUserData().getProfilePicturePath()).isEqualTo(generatedKey);

            verify(s3Service, never()).deleteFile(anyString());
            verify(s3Service).generateFileKey(userId, "avatar.jpg");
            verify(s3Service).uploadFile(picture, generatedKey);
            verify(userRepository).save(coreUser);
        }
    }

    // ========================================================================
    // getMyProfilePictureUrl() tests
    // ========================================================================

    @Nested
    @DisplayName("getMyProfilePictureUrl()")
    class GetMyProfilePictureUrlTests {

        @Test
        @DisplayName("Should return signed URL for the logged-in user's profile picture")
        void givenLoggedInUser_whenGetMyProfilePictureUrl_thenReturnSignedUrl() throws MalformedURLException {
            // given
            URL presignedUrl = new URL("https://bucket.s3.amazonaws.com/pic.jpg?signature=abc");

            when(permissionService.getLoggedInUser()).thenReturn(coreUser);
            when(s3Service.generatePresignedUrl("deharri/users/pic.jpg", 100)).thenReturn(presignedUrl);

            // when
            SignedUrlDto result = userService.getMyProfilePictureUrl();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUrl()).isEqualTo(presignedUrl);

            verify(permissionService).getLoggedInUser();
            verify(s3Service).generatePresignedUrl("deharri/users/pic.jpg", 100);
        }
    }

    // ========================================================================
    // isPasswordCorrect() tests
    // ========================================================================

    @Nested
    @DisplayName("isPasswordCorrect()")
    class IsPasswordCorrectTests {

        @Test
        @DisplayName("Should return true when given password matches encoded password")
        void givenMatchingPassword_whenIsPasswordCorrect_thenReturnTrue() {
            // given
            when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

            // when
            boolean result = userService.isPasswordCorrect("rawPassword", "encodedPassword");

            // then
            assertThat(result).isTrue();

            verify(passwordEncoder).matches("rawPassword", "encodedPassword");
        }

        @Test
        @DisplayName("Should return false when given password does not match encoded password")
        void givenNonMatchingPassword_whenIsPasswordCorrect_thenReturnFalse() {
            // given
            when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            // when
            boolean result = userService.isPasswordCorrect("wrongPassword", "encodedPassword");

            // then
            assertThat(result).isFalse();

            verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        }
    }

    // ========================================================================
    // getUserRoles() tests
    // ========================================================================

    @Nested
    @DisplayName("getUserRoles()")
    class GetUserRolesTests {

        @Test
        @DisplayName("Should return roles when username exists")
        void givenExistingUsername_whenGetUserRoles_thenReturnRoles() {
            // given
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(coreUser));

            // when
            List<UserRole> result = userService.getUserRoles("john_doe");

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(UserRole.ROLE_CONSUMER, UserRole.ROLE_WORKER);

            verify(userRepository).findByUsername("john_doe");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when username does not exist")
        void givenNonExistingUsername_whenGetUserRoles_thenThrowIllegalArgument() {
            // given
            when(userRepository.findByUsername("unknown_user")).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> userService.getUserRoles("unknown_user"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User not found with username: unknown_user");

            verify(userRepository).findByUsername("unknown_user");
        }
    }

    // ========================================================================
    // getIdFromUsername() tests
    // ========================================================================

    @Nested
    @DisplayName("getIdFromUsername()")
    class GetIdFromUsernameTests {

        @Test
        @DisplayName("Should return UUID when username exists")
        void givenExistingUsername_whenGetIdFromUsername_thenReturnUuid() {
            // given
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(coreUser));

            // when
            UUID result = userService.getIdFromUsername("john_doe");

            // then
            assertThat(result).isEqualTo(userId);

            verify(userRepository).findByUsername("john_doe");
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when username does not exist")
        void givenNonExistingUsername_whenGetIdFromUsername_thenThrowUsernameNotFound() {
            // given
            when(userRepository.findByUsername("ghost_user")).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> userService.getIdFromUsername("ghost_user"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessage("ghost_user");

            verify(userRepository).findByUsername("ghost_user");
        }
    }
}
