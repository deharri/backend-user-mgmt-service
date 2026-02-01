package com.deharri.ums.error.handler;

import com.deharri.ums.error.exception.AuthenticationException;
import com.deharri.ums.error.exception.AuthorizationException;
import com.deharri.ums.error.exception.FieldsValidationException;
import com.deharri.ums.error.response.BaseResponse;
import com.deharri.ums.error.response.DataIntegrityViolationExceptionResponse;
import com.deharri.ums.error.response.FieldsValidationExceptionResponse;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

/**
 * Global exception handler for the User Management Service.
 * <p>
 * Provides centralized exception handling for all REST controllers,
 * converting exceptions into consistent, user-friendly error responses.
 *
 * @author Ali Haris Chishti
 * @version 1.0.0
 */
@RestControllerAdvice
@Slf4j
@Hidden // Hide from Swagger UI - errors are documented on each endpoint
public class GlobalExceptionHandler {

    /**
     * Mapping of database constraint names to user-friendly messages.
     */
    private static final Map<String, String> CONSTRAINT_MESSAGES = Map.of(
            "uk_core_user_username", "Username already exists",
            "uk_core_user_phone_number", "Phone number already exists",
            "uk_core_user_email", "Email already exists",
            "uk_refresh_token_token", "Given Refresh Token already exists",
            "uk_refresh_token_username", "Refresh Token for given user already exists"
    );

    /**
     * Handles all unhandled exceptions.
     * This is the fallback handler for any unexpected errors.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleGeneralException(
            Exception e,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception occurred: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
        
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(new BaseResponse(
                        INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred. Please try again later.",
                        LocalDateTime.now(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles custom field validation exceptions.
     */
    @ExceptionHandler(FieldsValidationException.class)
    public ResponseEntity<FieldsValidationExceptionResponse> handleFieldsValidationException(
            FieldsValidationException e,
            HttpServletRequest request
    ) {
        log.warn("Validation failed: {} errors", e.getErrors().size());
        
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new FieldsValidationExceptionResponse(
                        BAD_REQUEST,
                        e.getMessage(),
                        e.getErrors(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles Spring's method argument validation exceptions.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FieldsValidationExceptionResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage() != null ? 
                                fieldError.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));
        
        log.warn("Validation failed for request to {}: {} errors", request.getRequestURI(), errors.size());
        
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new FieldsValidationExceptionResponse(
                        BAD_REQUEST,
                        "Validation failed",
                        errors,
                        request.getRequestURI()
                ));
    }

    /**
     * Handles authentication exceptions.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse> handleAuthenticationException(
            AuthenticationException e,
            HttpServletRequest request
    ) {
        log.warn("Authentication failed for request to {}: {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(new BaseResponse(
                        UNAUTHORIZED,
                        e.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles authorization exceptions.
     */
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<BaseResponse> handleAuthorizationException(
            AuthorizationException e,
            HttpServletRequest request
    ) {
        log.warn("Authorization failed for request to {}: {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity
                .status(FORBIDDEN)
                .body(new BaseResponse(
                        FORBIDDEN,
                        e.getMessage(),
                        LocalDateTime.now(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles Spring Security access denied exceptions.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse> handleAccessDeniedException(
            AccessDeniedException e,
            HttpServletRequest request
    ) {
        log.warn("Access denied for request to {}: {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity
                .status(FORBIDDEN)
                .body(new BaseResponse(
                        FORBIDDEN,
                        "You do not have permission to access this resource",
                        LocalDateTime.now(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles database constraint violations.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<DataIntegrityViolationExceptionResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException e,
            HttpServletRequest request
    ) {
        Throwable rootCause = e.getRootCause();
        String errorMessage = rootCause != null ? rootCause.getMessage() : e.getMessage();

        String userMessage = "Data integrity violation";
        String violation = "Constraint violation";

        if (errorMessage != null) {
            for (Map.Entry<String, String> entry : CONSTRAINT_MESSAGES.entrySet()) {
                if (errorMessage.toLowerCase().contains(entry.getKey().toLowerCase())) {
                    userMessage = entry.getValue();
                    violation = entry.getKey();
                    break;
                }
            }
        }

        log.warn("Data integrity violation on {}: {}", request.getRequestURI(), userMessage);

        return ResponseEntity
                .status(CONFLICT)
                .body(new DataIntegrityViolationExceptionResponse(
                        CONFLICT,
                        userMessage,
                        violation,
                        request.getRequestURI()
                ));
    }

    /**
     * Handles file upload size exceeded exceptions.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<BaseResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e,
            HttpServletRequest request
    ) {
        log.warn("File upload size exceeded on {}", request.getRequestURI());
        
        return ResponseEntity
                .status(PAYLOAD_TOO_LARGE)
                .body(new BaseResponse(
                        PAYLOAD_TOO_LARGE,
                        "File size exceeds the maximum allowed limit",
                        LocalDateTime.now(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles malformed JSON or unreadable request body.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e,
            HttpServletRequest request
    ) {
        log.warn("Malformed request body on {}: {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new BaseResponse(
                        BAD_REQUEST,
                        "Malformed request body. Please check your JSON syntax.",
                        LocalDateTime.now(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles unsupported HTTP methods.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request
    ) {
        log.warn("Method {} not supported for {}", e.getMethod(), request.getRequestURI());
        
        return ResponseEntity
                .status(METHOD_NOT_ALLOWED)
                .body(new BaseResponse(
                        METHOD_NOT_ALLOWED,
                        String.format("HTTP method '%s' is not supported for this endpoint", e.getMethod()),
                        LocalDateTime.now(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles unsupported media types.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<BaseResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException e,
            HttpServletRequest request
    ) {
        log.warn("Unsupported media type {} for {}", e.getContentType(), request.getRequestURI());
        
        return ResponseEntity
                .status(UNSUPPORTED_MEDIA_TYPE)
                .body(new BaseResponse(
                        UNSUPPORTED_MEDIA_TYPE,
                        String.format("Content type '%s' is not supported", e.getContentType()),
                        LocalDateTime.now(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles missing request parameters.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e,
            HttpServletRequest request
    ) {
        log.warn("Missing required parameter '{}' for {}", e.getParameterName(), request.getRequestURI());
        
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new BaseResponse(
                        BAD_REQUEST,
                        String.format("Required parameter '%s' is missing", e.getParameterName()),
                        LocalDateTime.now(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles type mismatch for method arguments.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request
    ) {
        log.warn("Type mismatch for parameter '{}' on {}", e.getName(), request.getRequestURI());
        
        String requiredType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown";
        
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new BaseResponse(
                        BAD_REQUEST,
                        String.format("Parameter '%s' should be of type '%s'", e.getName(), requiredType),
                        LocalDateTime.now(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles 404 not found exceptions.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<BaseResponse> handleNoHandlerFoundException(
            NoHandlerFoundException e,
            HttpServletRequest request
    ) {
        log.warn("No handler found for {} {}", e.getHttpMethod(), e.getRequestURL());
        
        return ResponseEntity
                .status(NOT_FOUND)
                .body(new BaseResponse(
                        NOT_FOUND,
                        String.format("Endpoint '%s' not found", e.getRequestURL()),
                        LocalDateTime.now(),
                        request.getRequestURI()
                ));
    }
}
