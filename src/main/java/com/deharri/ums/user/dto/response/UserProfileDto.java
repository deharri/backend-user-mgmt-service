package com.deharri.ums.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.UUID;

/**
 * Data Transfer Object for user profile information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "UserProfile",
        description = "User profile information"
)
public class UserProfileDto {

    @Schema(
            description = "Unique identifier for the user",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private UUID userId;

    @Schema(
            description = "User's first name",
            example = "John"
    )
    private String firstName;

    @Schema(
            description = "User's last name",
            example = "Doe"
    )
    private String lastName;

    @Schema(
            description = "User's email address",
            example = "john.doe@example.com"
    )
    private String email;

    @Schema(
            description = "User's phone number in international format",
            example = "+1234567890"
    )
    private String phoneNumber;

    @Schema(
            description = "Pre-signed URL for the user's profile picture (valid for limited time)",
            example = "https://bucket.s3.amazonaws.com/profile/123.jpg?signature=abc"
    )
    private URL profilePictureUrl;
}