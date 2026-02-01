package com.deharri.ums.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for refresh token requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "RefreshTokenRequest",
        description = "Request payload containing the refresh token"
)
public class RefreshTokenDto {

    @Schema(
            description = "The refresh token issued during login or token refresh",
            example = "dGhpcyBpcyBhIHNhbXBsZSByZWZyZXNoIHRva2VuIGZvciBkZW1vbnN0cmF0aW9u",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String token;
}
