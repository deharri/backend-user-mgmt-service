package com.deharri.ums.error.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Base error response returned by the API for error conditions.
 */
@Data
@AllArgsConstructor
@Schema(
        name = "ErrorResponse",
        description = "Standard error response for API errors"
)
public class BaseResponse {

    @Schema(
            description = "HTTP status code",
            example = "BAD_REQUEST"
    )
    protected final HttpStatus status;

    @Schema(
            description = "Human-readable error message",
            example = "Validation failed for the request"
    )
    protected final String message;

    @Schema(
            description = "Timestamp when the error occurred",
            example = "2024-01-15T10:30:00"
    )
    protected final LocalDateTime timestamp;

    @Schema(
            description = "Request path that caused the error",
            example = "/api/v1/auth/register"
    )
    protected final String path;
}
