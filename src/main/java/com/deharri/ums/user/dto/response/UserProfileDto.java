package com.deharri.ums.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.UUID;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder

public class UserProfileDto {

    private UUID userId;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private URL profilePictureUrl;

}