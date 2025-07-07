package com.deharri.ums.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // Pointcut for all methods in com.deharri.ums..service and ..controller packages
    @Pointcut(
            "execution(* com.deharri.ums..auth..*(..))" +
                    " || " +
                    "execution(* com.deharri.ums..user..*(..))" +
                    " || " +
                    "execution(* com.deharri.ums..exception..*(..))"
    )
    public void applicationPackagePointcut() {
        // Pointcut method
    }

    @Before("applicationPackagePointcut()")
    public void logMethodEntry(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        log.info("➡️ Entering method: {} with arguments: {}", methodName, Arrays.toString(args));
    }

    @AfterReturning(pointcut = "applicationPackagePointcut()", returning = "result")
    public void logMethodExit(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("⬅️ Exiting method: {} with return: {}", methodName, result);
    }

    @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "exception")
    public void logExceptions(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().toShortString();
        log.error("🔥 Exception in method: {} - {}", methodName, exception.getMessage(), exception);
    }
}
