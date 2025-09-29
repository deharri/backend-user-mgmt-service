package com.deharri.ums.user.dto.request;

import com.deharri.ums.annotations.ValidateClass;
import com.deharri.ums.user.dto.UserUpdateDto;
import com.deharri.ums.validation.annotation.ValidPhoneNumber;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ValidateClass
public class UserPhoneNoUpdateDto extends UserUpdateDto {

    @ValidPhoneNumber
    private String newPhoneNumber;

}