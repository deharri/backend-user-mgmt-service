package com.deharri.ums.validation.annotation;

import com.deharri.ums.validation.validator.UsernameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UsernameValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUsername {
    String message() default "Username must be 4 to 20 characters long";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
