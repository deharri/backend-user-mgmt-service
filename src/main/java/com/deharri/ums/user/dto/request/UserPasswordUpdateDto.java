package com.deharri.ums.user.dto.request;

import com.deharri.ums.annotations.ValidateClass;
import com.deharri.ums.user.dto.UserUpdateDto;
import com.deharri.ums.validation.annotation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Data Transfer Object for password update requests.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ValidateClass
@Schema(
        name = "PasswordUpdateRequest",
        description = "Request payload for updating user password"
)
public class UserPasswordUpdateDto extends UserUpdateDto {

    @ValidPassword
    @Schema(
            description = "New password (min 8 chars, must include uppercase, lowercase, number, and special character)",
            example = "NewSecureP@ss456",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String newPassword;
}
