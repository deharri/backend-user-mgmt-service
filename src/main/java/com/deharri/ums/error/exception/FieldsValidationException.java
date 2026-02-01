package com.deharri.ums.error.exception;

import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Exception thrown when field validation fails.
 * Supports both list-based and map-based error representations.
 */
@Getter
public class FieldsValidationException extends IllegalArgumentException {

    private final Map<String, String> errors;

    /**
     * Constructor with map of field errors.
     */
    public FieldsValidationException(Map<String, String> errors) {
        super("Data validation failed for field(s)");
        this.errors = errors;
    }

    /**
     * Constructor with list of error messages (for backward compatibility).
     * Errors are stored with generic keys like "error_0", "error_1", etc.
     */
    public FieldsValidationException(List<String> errorList) {
        super("Data validation failed for field(s)");
        this.errors = errorList.stream()
                .collect(Collectors.toMap(
                        error -> "error_" + errorList.indexOf(error),
                        error -> error
                ));
    }

    /**
     * Get errors as a list of messages.
     */
    public List<String> getErrorList() {
        return errors.values().stream().toList();
    }
}
