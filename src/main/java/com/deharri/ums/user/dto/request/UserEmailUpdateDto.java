package com.deharri.ums.user.dto.request;

import com.deharri.ums.annotations.ValidateClass;
import com.deharri.ums.user.dto.UserUpdateDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Data Transfer Object for email update requests.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ValidateClass
@Schema(
        name = "EmailUpdateRequest",
        description = "Request payload for updating user email address"
)
public class UserEmailUpdateDto extends UserUpdateDto {

    @Email(message = "Please provide a valid email address")
    @Schema(
            description = "New email address",
            example = "newemail@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String newEmail;
}