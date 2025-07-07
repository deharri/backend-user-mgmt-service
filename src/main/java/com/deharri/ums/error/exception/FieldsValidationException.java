package com.deharri.ums.error.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class FieldsValidationException extends IllegalArgumentException{

    private final List<String> errors;

    public FieldsValidationException(List<String> errors) {
        super("Data validation failed for field(s)");
        this.errors = errors;
    }

}
