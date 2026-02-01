package com.deharri.ums.user.dto.request;

import com.deharri.ums.annotations.ValidateClass;
import com.deharri.ums.user.dto.UserUpdateDto;
import com.deharri.ums.validation.annotation.ValidPhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Data Transfer Object for phone number update requests.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ValidateClass
@Schema(
        name = "PhoneNumberUpdateRequest",
        description = "Request payload for updating user phone number"
)
public class UserPhoneNoUpdateDto extends UserUpdateDto {

    @ValidPhoneNumber
    @Schema(
            description = "New phone number in international format",
            example = "+1987654321",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String newPhoneNumber;
}