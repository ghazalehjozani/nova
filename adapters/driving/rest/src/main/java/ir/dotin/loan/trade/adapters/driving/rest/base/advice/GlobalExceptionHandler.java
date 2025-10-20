package ir.dotin.loan.trade.adapters.driving.rest.base.advice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import ir.dotin.loan.trade.adapters.driving.rest.base.ServiceError;
import ir.dotin.loan.trade.adapters.driving.rest.base.response.ErrorResponse;

// TODO: Handle custom exception, fix messages, read messages from message.properties
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String SERVICE_PREFIX = "LOAN";

    // ==================== Authentication & Authorization (401, 403) ====================

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {

        log.warn("Authentication failed at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.of(SERVICE_PREFIX + "-0001", "نام کاربری یا رمز عبور نامعتبر است");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({AuthenticationException.class, InsufficientAuthenticationException.class})
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {

        log.warn("Authentication required at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.of(SERVICE_PREFIX + "-0002", "توکن احراز هویت وجود ندارد یا نامعتبر است");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.of(SERVICE_PREFIX + "-0003", "کاربر جاری مجوز اجرای این عملیات را ندارد");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // ==================== Validation Errors (400) ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("Validation failed at {}: {}", request.getRequestURI(), ex.getMessage());

        List<ServiceError> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(ServiceError.of(
                    "VAL-" + String.format("%04d", Math.abs(error.getField().hashCode() % 9999)),
                    error.getField() + ": " + error.getDefaultMessage()));
        }

        ErrorResponse error = ErrorResponse.of(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        log.warn("Constraint violation at {}: {}", request.getRequestURI(), ex.getMessage());

        List<ServiceError> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(ServiceError.of(
                    "VAL-"
                            + String.format(
                                    "%04d",
                                    Math.abs(violation
                                                    .getPropertyPath()
                                                    .toString()
                                                    .hashCode()
                                            % 9999)),
                    violation.getPropertyPath() + ": " + violation.getMessage()));
        }

        ErrorResponse error = ErrorResponse.of(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Invalid request body at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.of("VAL-0001", "قالب درخواست نامعتبر است");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        log.warn("Missing parameter at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.of("VAL-0002", "پارامتر مورد نیاز وجود ندارد: " + ex.getParameterName());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.warn("Type mismatch at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.of("VAL-0003", "نوع داده نامعتبر است برای پارامتر: " + ex.getName());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        log.warn("Invalid argument at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error =
                ErrorResponse.of("VAL-0004", ex.getMessage() != null ? ex.getMessage() : "مقدار ورودی نامعتبر است");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ==================== Resource Errors (404, 409) ====================

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex, HttpServletRequest request) {

        log.warn("Resource not found at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.of("NF-0001", "منبع درخواستی یافت نشد");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // ==================== Method Not Allowed (405) ====================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        log.warn("Method not allowed at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.of("METHOD-0001", "روش درخواست پشتیبانی نمی‌شود: " + ex.getMethod());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(
                        "Allow",
                        String.join(
                                ", ",
                                Objects.requireNonNull(ex.getSupportedHttpMethods())
                                        .toString()))
                .body(error);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

        log.warn("Media type not supported at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.of("METHOD-0002", "نوع محتوای درخواست پشتیبانی نمی‌شود");

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {

        log.warn("Invalid state at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                "BIZ-0002", ex.getMessage() != null ? ex.getMessage() : "وضعیت نامعتبر برای این عملیات");

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    // ==================== Downstream Service Errors (502, 503) ====================

    @ExceptionHandler({HttpClientErrorException.class, HttpServerErrorException.class})
    public ResponseEntity<ErrorResponse> handleHttpClientError(Exception ex, HttpServletRequest request) {

        log.error("Downstream service error at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.of("DOWNSTREAM-0001", "سرویس وابستگی در دسترس نیست");

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleResourceAccess(ResourceAccessException ex, HttpServletRequest request) {

        log.error("Resource access error at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.of("DOWNSTREAM-0002", "عدم دسترسی به سرویس وابستگی");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    // ==================== Internal Server Errors (500) ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {

        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.of("SYS-9999", "خطای غیرمنتظره رخ داده است");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
