package com.deharri.ums.user;

import com.deharri.ums.amazon.dto.SignedUrlDto;
import com.deharri.ums.error.response.BaseResponse;
import com.deharri.ums.user.dto.request.UserEmailUpdateDto;
import com.deharri.ums.user.dto.request.UserPasswordUpdateDto;
import com.deharri.ums.user.dto.request.UserPhoneNoUpdateDto;
import com.deharri.ums.user.dto.response.ResponseMessageDto;
import com.deharri.ums.user.dto.response.UserProfileDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for user profile management.
 * <p>
 * Provides endpoints for viewing and updating user profiles, including
 * profile pictures, email, phone number, and password management.
 * All endpoints require authentication except the public user list.
 *
 * @author Ali Haris Chishti
 * @version 1.0.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * Get all user profiles (public endpoint).
     *
     * @return list of all user profiles
     */
    @Tag(name = "User Profile")
    @Operation(
            summary = "Get all user profiles",
            description = """
                    Retrieves a list of all registered user profiles.
                    
                    **Note:** This is a public endpoint for user discovery.
                    Sensitive information like email and phone are partially masked.
                    """,
            security = {}  // No security - public endpoint
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved user list",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = UserProfileDto.class))
                    )
            )
    })
    @GetMapping("")
    public ResponseEntity<List<UserProfileDto>> getAllUserProfiles() {
        log.debug("Fetching all user profiles");
        return ResponseEntity.ok(userService.getAllUserProfiles());
    }

    /**
     * Get currently authenticated user's profile.
     *
     * @return the current user's profile
     */
    @Tag(name = "User Profile")
    @Operation(
            summary = "Get my profile",
            description = """
                    Retrieves the profile of the currently authenticated user.
                    
                    Returns complete profile information including:
                    - User ID
                    - Full name
                    - Email address
                    - Phone number
                    - Profile picture URL (if set)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved profile",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserProfileDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BaseResponse.class)
                    )
            )
    })
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMyProfile() {
        log.debug("Fetching current user's profile");
        return ResponseEntity.ok(userService.getMyProfile());
    }

    /**
     * Get a specific user's profile by UUID.
     *
     * @param uuid the user's unique identifier
     * @return the requested user's profile
     */
    @Tag(name = "User Profile")
    @Operation(
            summary = "Get user profile by ID",
            description = "Retrieves the public profile of a specific user by their UUID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved profile",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserProfileDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BaseResponse.class)
                    )
            )
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<UserProfileDto> getUserProfile(
            @Parameter(
                    description = "User's unique identifier (UUID)",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID uuid
    ) {
        log.debug("Fetching profile for user ID: {}", uuid);
        return ResponseEntity.ok(userService.getUserProfile(uuid));
    }

    /**
     * Update current user's password.
     *
     * @param dto password update details
     * @return confirmation message
     */
    @Tag(name = "Password Management")
    @Operation(
            summary = "Update password",
            description = """
                    Updates the password for the currently authenticated user.
                    
                    **Requirements:**
                    - Must provide current password for verification
                    - New password must meet complexity requirements:
                      - Minimum 8 characters
                      - At least one uppercase letter
                      - At least one lowercase letter
                      - At least one number
                      - At least one special character
                    - New password cannot be the same as current password
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseMessageDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "Password updated successfully"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid password or validation failed",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BaseResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Current password is incorrect",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BaseResponse.class)
                    )
            )
    })
    @PutMapping("/password")
    public ResponseEntity<ResponseMessageDto> updatePassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Password update request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserPasswordUpdateDto.class))
            )
            @RequestBody UserPasswordUpdateDto dto
    ) {
        log.info("Processing password update request");
        return ResponseEntity.ok(userService.updatePassword(dto));
    }

    /**
     * Update current user's email address.
     *
     * @param dto email update details
     * @return confirmation message
     */
    @Tag(name = "Contact Information")
    @Operation(
            summary = "Update email address",
            description = """
                    Updates the email address for the currently authenticated user.
                    
                    **Note:** A verification email may be sent to the new address.
                    The email must be unique across all users.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseMessageDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid email format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BaseResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already in use",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BaseResponse.class)
                    )
            )
    })
    @PutMapping("/email")
    public ResponseEntity<ResponseMessageDto> updateEmail(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email update request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserEmailUpdateDto.class))
            )
            @RequestBody UserEmailUpdateDto dto
    ) {
        log.info("Processing email update request");
        return ResponseEntity.ok(userService.updateEmail(dto));
    }

    /**
     * Update current user's phone number.
     *
     * @param dto phone number update details
     * @return confirmation message
     */
    @Tag(name = "Contact Information")
    @Operation(
            summary = "Update phone number",
            description = """
                    Updates the phone number for the currently authenticated user.
                    
                    **Format:** International format with country code (e.g., +1234567890)
                    The phone number must be unique across all users.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Phone number updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseMessageDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid phone number format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BaseResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Phone number already in use",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BaseResponse.class)
                    )
            )
    })
    @PutMapping("/phone")
    public ResponseEntity<ResponseMessageDto> updatePhoneNo(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Phone number update request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserPhoneNoUpdateDto.class))
            )
            @RequestBody UserPhoneNoUpdateDto dto
    ) {
        log.info("Processing phone number update request");
        return ResponseEntity.ok(userService.updatePhoneNo(dto));
    }

    /**
     * Upload a new profile picture.
     *
     * @param picture the image file to upload
     * @return confirmation message
     */
    @Tag(name = "Profile Picture")
    @Operation(
            summary = "Upload profile picture",
            description = """
                    Uploads a new profile picture for the currently authenticated user.
                    
                    **Accepted formats:** JPEG, PNG, GIF, WebP
                    **Maximum size:** 5 MB
                    **Recommended dimensions:** 400x400 pixels (square)
                    
                    The image is stored in Amazon S3 and a signed URL is generated for access.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Profile picture uploaded successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseMessageDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid file format or size",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BaseResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "413",
                    description = "File too large",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BaseResponse.class)
                    )
            )
    })
    @PostMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseMessageDto> updateProfilePicture(
            @Parameter(
                    description = "Profile picture file (JPEG, PNG, GIF, or WebP, max 5MB)",
                    required = true
            )
            @NotNull @RequestPart MultipartFile picture
    ) {
        log.info("Processing profile picture upload, size: {} bytes", picture.getSize());
        return ResponseEntity.status(201).body(userService.updateProfilePicture(picture));
    }

    /**
     * Get current user's profile picture URL.
     *
     * @return signed URL for the profile picture
     */
    @Tag(name = "Profile Picture")
    @Operation(
            summary = "Get my profile picture URL",
            description = """
                    Retrieves a pre-signed URL for the current user's profile picture.
                    
                    The URL is valid for a limited time (typically 1 hour) and provides
                    direct access to the image stored in S3.
                    
                    Returns null if no profile picture has been uploaded.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved profile picture URL",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SignedUrlDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No profile picture found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BaseResponse.class)
                    )
            )
    })
    @GetMapping("/picture")
    public ResponseEntity<SignedUrlDto> getMyProfilePictureUrl() {
        log.debug("Fetching profile picture URL for current user");
        return ResponseEntity.ok(userService.getMyProfilePictureUrl());
    }
}
