package com.deharri.ums.user.dto.request;

import com.deharri.ums.annotations.ValidateClass;
import com.deharri.ums.user.dto.UserUpdateDto;
import jakarta.validation.constraints.Email;
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
public class UserEmailUpdateDto extends UserUpdateDto {

    @Email
    private String newEmail;

}