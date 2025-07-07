package com.deharri.ums.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuthResponseDto {

    private String accessToken;

    private String refreshToken;
}
