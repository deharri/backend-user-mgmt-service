package com.deharri.ums.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionMessage {

    USER_NOT_FOUND_IN_TOKEN("Invalid token, user not found"),
    USER_NOT_FOUND_WITH_USERNAME("User not found with given username"),
    INCORRECT_PASSWORD("Password is Incorrect"),
    REFRESH_TOKEN_NOT_FOUND("Refresh token not found"),
    ACCESS_TOKEN_NOT_FOUND("Refresh token not found"),
    REFRESH_TOKEN_EXPIRED("Refresh token expired");

    private final String text;
}
