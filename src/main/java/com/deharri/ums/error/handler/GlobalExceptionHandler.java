package com.deharri.ums.error.handler;

import com.deharri.ums.error.exception.AuthenticationException;
import com.deharri.ums.error.exception.AuthorizationException;
import com.deharri.ums.error.exception.FieldsValidationException;
import com.deharri.ums.error.response.BaseResponse;
import com.deharri.ums.error.response.DataIntegrityViolationExceptionResponse;
import com.deharri.ums.error.response.FieldsValidationExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    private static final Map<String, String> CONSTRAINT_MESSAGES = Map.of(
            "uk_core_user_username", "Username already exists",
            "uk_core_user_phone_number", "Phone number already exists",
            "uk_refresh_token_token", "Given Refresh Token already exists",
            "uk_refresh_token_username", "Refresh Token for given user already exists"
    );

    @ExceptionHandler(Exception.class)
    private ResponseEntity<BaseResponse> handleGeneralException(
            Exception e,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception: {}", e.getMessage(), e);
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        new BaseResponse(
                                INTERNAL_SERVER_ERROR,
                                e.getMessage(),
                                LocalDateTime.now(),
                                request.getRequestURI()
                        )
                );
    }


    @ExceptionHandler(FieldsValidationException.class)
    private ResponseEntity<FieldsValidationExceptionResponse> handleFieldsValidationException(
            FieldsValidationException e,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                        new FieldsValidationExceptionResponse(
                                BAD_REQUEST,
                                e.getMessage(),
                                e.getErrors(),
                                request.getRequestURI()
                        )
                );
    }


    @ExceptionHandler(AuthenticationException.class)
    private ResponseEntity<BaseResponse> handleAuthenticationException(
            AuthenticationException e,
            HttpServletRequest request
    ) {
        log.warn("Authentication error: {}", e.getMessage());
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        new BaseResponse(
                                UNAUTHORIZED,
                                e.getMessage(),
                                LocalDateTime.now(),
                                request.getRequestURI()
                        )
                );
    }

    @ExceptionHandler(AuthorizationException.class)
    private ResponseEntity<BaseResponse> handleAuthorizationException(
            AuthorizationException e,
            HttpServletRequest request
    ) {
        log.warn("Authorization error: {}", e.getMessage());
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        new BaseResponse(
                                UNAUTHORIZED,
                                e.getMessage(),
                                LocalDateTime.now(),
                                request.getRequestURI()
                        )
                );
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    private ResponseEntity<DataIntegrityViolationExceptionResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException e,
            HttpServletRequest request
    ) {

        Throwable rootCause = e.getRootCause();
        String errorMessage = rootCause != null ? rootCause.getMessage() : e.getMessage();

        // Default fallback message
        String userMessage = "Data integrity violation";
        String violation = "Data integrity violation";

        if (errorMessage != null) {
            for (Map.Entry<String, String> entry : CONSTRAINT_MESSAGES.entrySet()) {
                if (errorMessage.contains(entry.getKey())) {
                    userMessage = CONSTRAINT_MESSAGES.get(entry.getKey());
                    violation = entry.getValue();
                    break;
                }
            }
        }

        log.warn("Constraint violation: {}", errorMessage);


        return ResponseEntity
                .status(CONFLICT)
                .body(
                        new DataIntegrityViolationExceptionResponse(
                                CONFLICT,
                                errorMessage,
                                violation,
                                request.getRequestURI()
                        )
                );
    }

}
