package com.deharri.ums.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserDto {
    private UUID userId;
    private String username;
    private String firstName;
    private String lastName;
}
