package com.deharri.ums.error.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BaseResponse {

    protected final HttpStatus status;

    protected final String message;

    protected final LocalDateTime timestamp;

    protected final String path;
}
