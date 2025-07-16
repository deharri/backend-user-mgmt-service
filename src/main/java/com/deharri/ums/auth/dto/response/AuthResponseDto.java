package com.deharri.ums.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuthResponseDto {

    private String accessToken;

    private String refreshToken;
}
