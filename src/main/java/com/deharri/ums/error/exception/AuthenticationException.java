package com.deharri.ums.error.exception;

import com.deharri.ums.enums.ExceptionMessage;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(ExceptionMessage message) {
        super(message.getText());
    }
}
