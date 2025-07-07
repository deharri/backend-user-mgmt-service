package com.deharri.ums.error.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class DataIntegrityViolationExceptionResponse extends BaseResponse{

    private final String violation;

    public DataIntegrityViolationExceptionResponse(HttpStatus status, String message, String violation, String path) {
        super(status, message, LocalDateTime.now(), path);
        this.violation = violation;
    }

}
