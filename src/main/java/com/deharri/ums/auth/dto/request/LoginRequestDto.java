package com.deharri.ums.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user login requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "LoginRequest",
        description = "Request payload for user authentication"
)
public class LoginRequestDto {

    @Schema(
            description = "User's username",
            example = "john_doe",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @Schema(
            description = "User's password",
            example = "SecureP@ss123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;

    @Schema(
            description = "If true, extends the session/token validity period",
            example = "false",
            defaultValue = "false"
    )
    private boolean rememberMe;
}
