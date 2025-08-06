package com.deharri.ums.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserProfileDto {

    private UUID uuid;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private String profilePictureUrl;

}