package com.deharri.ums.user;

import com.deharri.ums.amazon.dto.SignedUrlDto;
import com.deharri.ums.config.security.jwt.JwtService;
import com.deharri.ums.user.dto.request.UserEmailUpdateDto;
import com.deharri.ums.user.dto.request.UserPasswordUpdateDto;
import com.deharri.ums.user.dto.request.UserPhoneNoUpdateDto;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.user.dto.response.UserProfileDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private UUID userId;
    private UserProfileDto userProfileDto;

    @BeforeEach
    void setUp() throws Exception {
        userId = UUID.randomUUID();

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
    // GET /api/v1/users - getAllUserProfiles()
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/users")
    class GetAllUserProfilesTests {

        @Test
        @DisplayName("Should return 200 with list of user profiles")
        void givenUsersExist_whenGetAllUserProfiles_thenReturn200WithList() throws Exception {
            // given
            UserProfileDto secondProfile = UserProfileDto.builder()
                    .userId(UUID.randomUUID())
                    .firstName("Jane")
                    .lastName("Doe")
                    .email("jane@example.com")
                    .phoneNumber("+9876543210")
                    .build();

            when(userService.getAllUserProfiles()).thenReturn(List.of(userProfileDto, secondProfile));

            // when / then
            mockMvc.perform(get("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].firstName", is("John")))
                    .andExpect(jsonPath("$[0].lastName", is("Doe")))
                    .andExpect(jsonPath("$[0].email", is("john@example.com")))
                    .andExpect(jsonPath("$[1].firstName", is("Jane")))
                    .andExpect(jsonPath("$[1].lastName", is("Doe")));

            verify(userService).getAllUserProfiles();
        }
    }

    // ========================================================================
    // GET /api/v1/users/me - getMyProfile()
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/users/me")
    class GetMyProfileTests {

        @Test
        @DisplayName("Should return 200 with current user's profile")
        void givenLoggedInUser_whenGetMyProfile_thenReturn200WithProfile() throws Exception {
            // given
            when(userService.getMyProfile()).thenReturn(userProfileDto);

            // when / then
            mockMvc.perform(get("/api/v1/users/me")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId", is(userId.toString())))
                    .andExpect(jsonPath("$.firstName", is("John")))
                    .andExpect(jsonPath("$.lastName", is("Doe")))
                    .andExpect(jsonPath("$.email", is("john@example.com")))
                    .andExpect(jsonPath("$.phoneNumber", is("+1234567890")));

            verify(userService).getMyProfile();
        }
    }

    // ========================================================================
    // GET /api/v1/users/{uuid} - getUserProfile()
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/users/{uuid}")
    class GetUserProfileTests {

        @Test
        @DisplayName("Should return 200 with user profile for valid UUID")
        void givenValidUuid_whenGetUserProfile_thenReturn200WithProfile() throws Exception {
            // given
            when(userService.getUserProfile(userId)).thenReturn(userProfileDto);

            // when / then
            mockMvc.perform(get("/api/v1/users/{uuid}", userId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId", is(userId.toString())))
                    .andExpect(jsonPath("$.firstName", is("John")))
                    .andExpect(jsonPath("$.lastName", is("Doe")))
                    .andExpect(jsonPath("$.email", is("john@example.com")))
                    .andExpect(jsonPath("$.phoneNumber", is("+1234567890")));

            verify(userService).getUserProfile(userId);
        }
    }

    // ========================================================================
    // PUT /api/v1/users/password - updatePassword()
    // ========================================================================

    @Nested
    @DisplayName("PUT /api/v1/users/password")
    class UpdatePasswordTests {

        @Test
        @DisplayName("Should return 200 with success message when password is valid")
        void givenValidDto_whenUpdatePassword_thenReturn200WithMessage() throws Exception {
            // given
            UserPasswordUpdateDto dto = UserPasswordUpdateDto.builder()
                    .oldPassword("OldP@ss123")
                    .newPassword("NewSecureP@ss456")
                    .build();

            ResponseMessageDto responseMessage = new ResponseMessageDto("Password Updated Successfully!");
            when(userService.updatePassword(any(UserPasswordUpdateDto.class))).thenReturn(responseMessage);

            // when / then
            mockMvc.perform(put("/api/v1/users/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is("Password Updated Successfully!")));

            verify(userService).updatePassword(any(UserPasswordUpdateDto.class));
        }
    }

    // ========================================================================
    // PUT /api/v1/users/email - updateEmail()
    // ========================================================================

    @Nested
    @DisplayName("PUT /api/v1/users/email")
    class UpdateEmailTests {

        @Test
        @DisplayName("Should return 200 with success message when email is valid")
        void givenValidDto_whenUpdateEmail_thenReturn200WithMessage() throws Exception {
            // given
            UserEmailUpdateDto dto = UserEmailUpdateDto.builder()
                    .oldPassword("OldP@ss123")
                    .newEmail("newemail@example.com")
                    .build();

            ResponseMessageDto responseMessage = new ResponseMessageDto("Email Updated Successfully!");
            when(userService.updateEmail(any(UserEmailUpdateDto.class))).thenReturn(responseMessage);

            // when / then
            mockMvc.perform(put("/api/v1/users/email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is("Email Updated Successfully!")));

            verify(userService).updateEmail(any(UserEmailUpdateDto.class));
        }
    }

    // ========================================================================
    // PUT /api/v1/users/phone - updatePhoneNo()
    // ========================================================================

    @Nested
    @DisplayName("PUT /api/v1/users/phone")
    class UpdatePhoneNoTests {

        @Test
        @DisplayName("Should return 200 with success message when phone number is valid")
        void givenValidDto_whenUpdatePhoneNo_thenReturn200WithMessage() throws Exception {
            // given
            UserPhoneNoUpdateDto dto = UserPhoneNoUpdateDto.builder()
                    .oldPassword("OldP@ss123")
                    .newPhoneNumber("+9876543210")
                    .build();

            ResponseMessageDto responseMessage = new ResponseMessageDto("Phone Number Updated Successfully!");
            when(userService.updatePhoneNo(any(UserPhoneNoUpdateDto.class))).thenReturn(responseMessage);

            // when / then
            mockMvc.perform(put("/api/v1/users/phone")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is("Phone Number Updated Successfully!")));

            verify(userService).updatePhoneNo(any(UserPhoneNoUpdateDto.class));
        }
    }

    // ========================================================================
    // POST /api/v1/users/picture - updateProfilePicture()
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/users/picture")
    class UpdateProfilePictureTests {

        @Test
        @DisplayName("Should return 201 with success message when file is valid")
        void givenValidFile_whenUpdateProfilePicture_thenReturn201WithMessage() throws Exception {
            // given
            MockMultipartFile picture = new MockMultipartFile(
                    "picture",
                    "avatar.png",
                    MediaType.IMAGE_PNG_VALUE,
                    "fake-image-content".getBytes()
            );

            ResponseMessageDto responseMessage = new ResponseMessageDto("Profile Picture Updated Successfully!");
            when(userService.updateProfilePicture(any())).thenReturn(responseMessage);

            // when / then
            mockMvc.perform(multipart("/api/v1/users/picture")
                            .file(picture)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message", is("Profile Picture Updated Successfully!")));

            verify(userService).updateProfilePicture(any());
        }
    }

    // ========================================================================
    // GET /api/v1/users/picture - getMyProfilePictureUrl()
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/users/picture")
    class GetMyProfilePictureUrlTests {

        @Test
        @DisplayName("Should return 200 with signed URL for profile picture")
        void givenLoggedInUser_whenGetMyProfilePictureUrl_thenReturn200WithUrl() throws Exception {
            // given
            URL presignedUrl = new URL("https://bucket.s3.amazonaws.com/pic.jpg?signature=abc");
            SignedUrlDto signedUrlDto = new SignedUrlDto(presignedUrl);
            when(userService.getMyProfilePictureUrl()).thenReturn(signedUrlDto);

            // when / then
            mockMvc.perform(get("/api/v1/users/picture")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.url", is("https://bucket.s3.amazonaws.com/pic.jpg?signature=abc")));

            verify(userService).getMyProfilePictureUrl();
        }
    }
}
