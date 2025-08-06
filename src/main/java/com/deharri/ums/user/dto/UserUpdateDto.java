package com.deharri.ums.user.dto;

import com.deharri.ums.annotations.CheckPassword;
import com.deharri.ums.annotations.ValidateClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserUpdateDto {


    protected String oldPassword;

}
