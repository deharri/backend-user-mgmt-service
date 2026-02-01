package com.deharri.ums.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base class for user update operations that require password verification.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Base update request requiring current password verification")
public class UserUpdateDto {

    @Schema(
            description = "Current password for verification",
            example = "CurrentP@ss123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    protected String oldPassword;
}
