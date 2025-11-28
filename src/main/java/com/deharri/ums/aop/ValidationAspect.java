package com.deharri.ums.aop;

import com.deharri.ums.annotations.ValidateClass;
import com.deharri.ums.enums.ExceptionMessage;
import com.deharri.ums.error.exception.AuthenticationException;
import com.deharri.ums.error.exception.FieldsValidationException;
import com.deharri.ums.permission.PermissionService;
import com.deharri.ums.user.UserService;
import com.deharri.ums.user.dto.UserUpdateDto;
import com.deharri.ums.user.entity.CoreUser;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ValidationAspect {


    private final Validator validator;

    private final UserService userService;
    private final PermissionService permissionService;

    @Before("@annotation(com.deharri.ums.annotations.ValidateArguments)")
    public void validateMethodArguments(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        for (Object arg : args) {
            if (arg == null) continue;

            if (!arg.getClass().isAnnotationPresent(ValidateClass.class))
                continue;

            Set<ConstraintViolation<Object>> violations = validator.validate(arg);
            List<String> errorMessages = new ArrayList<>();
            if (!violations.isEmpty()) {
                for (ConstraintViolation<Object> violation : violations) {
                    errorMessages.add(violation.getMessage());
                }
                throw new FieldsValidationException(errorMessages);
            }
        }
    }

    @Before("@annotation(com.deharri.ums.annotations.CheckPassword)")
    public void validatePassword(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        UserUpdateDto updateDto = (UserUpdateDto) args[0];
        CoreUser currentUser = permissionService.getLoggedInUser();
        if (!userService.isPasswordCorrect(updateDto.getOldPassword(), currentUser.getPassword()))
            throw new AuthenticationException(ExceptionMessage.OLD_PASSWORD_NOT_CORRECT);
    }
}
