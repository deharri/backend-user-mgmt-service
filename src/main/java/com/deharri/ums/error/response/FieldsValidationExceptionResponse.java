package com.deharri.ums.error.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class FieldsValidationExceptionResponse extends BaseResponse{

    private final List<String> errors;

    public FieldsValidationExceptionResponse(HttpStatus status, String message, List<String> errors, String path) {
        super(status, message, LocalDateTime.now(), path);
        this.errors = errors;
    }

}
