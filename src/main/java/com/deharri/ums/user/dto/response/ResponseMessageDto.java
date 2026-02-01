package com.deharri.ums.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic response DTO for simple message responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "ResponseMessage",
        description = "Simple response containing a message"
)
public class ResponseMessageDto {

    @Schema(
            description = "Response message",
            example = "Operation completed successfully"
    )
    private String message;
}
