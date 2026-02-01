package com.deharri.ums.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for authentication responses.
 * Contains JWT access token and refresh token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "AuthResponse",
        description = "Response containing authentication tokens"
)
public class AuthResponseDto {

    @Schema(
            description = "JWT access token for API authentication. Include in Authorization header as 'Bearer <token>'",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
    )
    private String accessToken;

    @Schema(
            description = "Refresh token for obtaining new access tokens when they expire",
            example = "dGhpcyBpcyBhIHNhbXBsZSByZWZyZXNoIHRva2VuIGZvciBkZW1vbnN0cmF0aW9u"
    )
    private String refreshToken;
}
