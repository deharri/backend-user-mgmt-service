package com.deharri.ums.auth.dto.request;

import com.deharri.ums.annotations.ValidateClass;
import com.deharri.ums.validation.annotation.ValidPassword;
import com.deharri.ums.validation.annotation.ValidPhoneNumber;
import com.deharri.ums.validation.annotation.ValidUsername;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user registration requests.
 * Contains all required fields for creating a new user account.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidateClass
@Schema(
        name = "RegisterRequest",
        description = "Request payload for user registration"
)
public class RegisterRequestDto {

    @ValidPhoneNumber
    @Schema(
            description = "User's phone number in international format",
            example = "+1234567890",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String phoneNumber;

    @ValidUsername
    @Schema(
            description = "Unique username (4-20 characters, alphanumeric and underscores)",
            example = "john_doe",
            minLength = 4,
            maxLength = 20,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @ValidPassword
    @Schema(
            description = "Password (min 8 chars, must include uppercase, lowercase, number, and special character)",
            example = "SecureP@ss123",
            minLength = 8,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be at most 50 characters")
    @Pattern(
            regexp = "^[A-Za-z]+$",
            message = "First name must contain only letters"
    )
    @Schema(
            description = "User's first name (letters only)",
            example = "John",
            maxLength = 50,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    @Pattern(
            regexp = "^[A-Za-z]+$",
            message = "Last name must contain only letters"
    )
    @Schema(
            description = "User's last name (letters only)",
            example = "Doe",
            maxLength = 50,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String lastName;

    @Schema(
            description = "If true, extends the refresh token validity period to 30 days",
            example = "false",
            defaultValue = "false"
    )
    private boolean rememberMe;
}
