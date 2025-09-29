package com.deharri.ums.aop;

import com.deharri.ums.kafka.log.LogMessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final LogMessageService logMessageService;

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

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controller() {}

    @Around("controller()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        log.info("⬇️ REQUEST [{} {}] - Args: {}", request.getMethod(), request.getRequestURI(), Arrays.toString(joinPoint.getArgs()));
        logMessageService.sendEndpointLogMessage(request.getRequestURI(), request.getMethod(), LogLevel.INFO);

        Object result = joinPoint.proceed();

        log.info("⬆️ RESPONSE [{} {}] - Return: {}", request.getMethod(), request.getRequestURI(), result);

        return result;
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
