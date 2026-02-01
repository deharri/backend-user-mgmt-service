package com.deharri.ums.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.UUID;

/**
 * Aspect for comprehensive application logging.
 * <p>
 * This aspect provides:
 * <ul>
 *     <li>Request/Response logging for all REST controllers</li>
 *     <li>Method entry/exit logging for service layer</li>
 *     <li>Exception logging with full stack traces</li>
 *     <li>Performance timing metrics</li>
 *     <li>Correlation IDs for request tracing (MDC)</li>
 * </ul>
 *
 * @author Ali Haris Chishti
 * @version 1.0.0
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String REQUEST_ID_KEY = "requestId";

    /**
     * Pointcut for application packages.
     */
    @Pointcut("execution(* com.deharri.ums..auth..*(..)) || " +
              "execution(* com.deharri.ums..user..*(..)) || " +
              "execution(* com.deharri.ums..worker..*(..)) || " +
              "execution(* com.deharri.ums..amazon..*(..))")
    public void applicationPackagePointcut() {
        // Pointcut method
    }

    /**
     * Pointcut for REST controllers.
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerPointcut() {
        // Pointcut method
    }

    /**
     * Pointcut for service layer.
     */
    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void servicePointcut() {
        // Pointcut method
    }

    /**
     * Pointcut for repository layer.
     */
    @Pointcut("within(@org.springframework.stereotype.Repository *)")
    public void repositoryPointcut() {
        // Pointcut method
    }

    /**
     * Around advice for controller methods.
     * Logs request details, timing, and response.
     */
    @Around("controllerPointcut()")
    public Object logControllerCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        
        // Generate and set correlation ID for request tracing
        String correlationId = generateCorrelationId(request);
        MDC.put(CORRELATION_ID_KEY, correlationId);
        MDC.put(REQUEST_ID_KEY, UUID.randomUUID().toString().substring(0, 8));

        String methodName = joinPoint.getSignature().toShortString();
        String clientIp = getClientIpAddress(request);
        
        log.info("▶ REQUEST  | {} {} | Client: {} | Method: {} | Args: {}",
                request.getMethod(),
                request.getRequestURI(),
                clientIp,
                methodName,
                maskSensitiveData(joinPoint.getArgs()));

        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("◀ RESPONSE | {} {} | Duration: {}ms | Status: SUCCESS",
                    request.getMethod(),
                    request.getRequestURI(),
                    duration);
            
            // Log performance warning for slow requests
            if (duration > 1000) {
                log.warn("⚠ SLOW REQUEST | {} {} | Duration: {}ms exceeded threshold",
                        request.getMethod(),
                        request.getRequestURI(),
                        duration);
            }
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("✖ RESPONSE | {} {} | Duration: {}ms | Status: ERROR | Exception: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    duration,
                    e.getMessage());
            throw e;
        } finally {
            MDC.remove(CORRELATION_ID_KEY);
            MDC.remove(REQUEST_ID_KEY);
        }
    }

    /**
     * Before advice for service methods.
     */
    @Before("servicePointcut() && applicationPackagePointcut()")
    public void logServiceMethodEntry(JoinPoint joinPoint) {
        if (log.isDebugEnabled()) {
            String methodName = joinPoint.getSignature().toShortString();
            log.debug("→ ENTERING | {} | Args: {}",
                    methodName,
                    maskSensitiveData(joinPoint.getArgs()));
        }
    }

    /**
     * After returning advice for service methods.
     */
    @AfterReturning(pointcut = "servicePointcut() && applicationPackagePointcut()", returning = "result")
    public void logServiceMethodExit(JoinPoint joinPoint, Object result) {
        if (log.isDebugEnabled()) {
            String methodName = joinPoint.getSignature().toShortString();
            log.debug("← EXITING  | {} | Result: {}",
                    methodName,
                    maskSensitiveResult(result));
        }
    }

    /**
     * After throwing advice for exception logging.
     */
    @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().toShortString();
        log.error("✖ EXCEPTION | {} | Type: {} | Message: {}",
                methodName,
                exception.getClass().getSimpleName(),
                exception.getMessage());
        
        // Log full stack trace at debug level to avoid log pollution
        if (log.isDebugEnabled()) {
            log.debug("Stack trace for exception in {}", methodName, exception);
        }
    }

    /**
     * Generates or retrieves correlation ID from request headers.
     */
    private String generateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }

    /**
     * Extracts client IP address, handling proxy headers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * Masks sensitive data in method arguments.
     */
    private String maskSensitiveData(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) return "null";
                    String argString = arg.toString();
                    String lowerArg = argString.toLowerCase();
                    
                    // Mask password fields
                    if (lowerArg.contains("password")) {
                        return argString.replaceAll("(?i)(password[^,}]*[=:]\\s*)([^,}]+)", "$1***");
                    }
                    // Mask token fields
                    if (lowerArg.contains("token")) {
                        return argString.replaceAll("(?i)(token[^,}]*[=:]\\s*)([^,}]+)", "$1***");
                    }
                    return argString;
                })
                .toList()
                .toString();
    }

    /**
     * Masks sensitive data in return values.
     */
    private String maskSensitiveResult(Object result) {
        if (result == null) return "null";
        
        String resultString = result.toString();
        String lowerResult = resultString.toLowerCase();
        
        // Mask tokens in response
        if (lowerResult.contains("token") || lowerResult.contains("accesstoken") || lowerResult.contains("refreshtoken")) {
            return resultString
                    .replaceAll("(?i)(accessToken[^,}]*[=:]\\s*)([^,}]+)", "$1***")
                    .replaceAll("(?i)(refreshToken[^,}]*[=:]\\s*)([^,}]+)", "$1***")
                    .replaceAll("(?i)(token[^,}]*[=:]\\s*)([^,}]+)", "$1***");
        }
        
        return resultString;
    }
}
