package com.deharri.ums.auth.dto.request;

import com.deharri.ums.validation.annotation.ValidPhoneNumber;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendOtpRequestDto {

    @ValidPhoneNumber
    private String phoneNumber;
}
