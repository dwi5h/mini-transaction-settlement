package com.dwish.minitrxsettlement.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({HandlerMethodValidationException.class, ConstraintViolationException.class})
    public ProblemDetail handleMethodValidationErrors(Exception ex, HttpServletRequest request) {
        log.warn("Gagal memvalidasi parameter permintaan pada URI: {}", request.getRequestURI());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Parameter query yang Anda masukkan tidak valid"
        );
        problemDetail.setTitle("Invalid Request Parameters");

        Map<String, String> errors = new HashMap<>();

        if (ex instanceof HandlerMethodValidationException validationEx) {
            validationEx.getParameterValidationResults().forEach(result -> {
                String paramName = result.getMethodParameter().getParameterName();
                result.getResolvableErrors().forEach(error ->
                        errors.put(paramName, error.getDefaultMessage())
                );
            });
        }
        else if (ex instanceof ConstraintViolationException violationEx) {
            violationEx.getConstraintViolations().forEach(violation -> {
                String paramName = violation.getPropertyPath().toString();
                errors.put(paramName, violation.getMessage());
            });
        }

        problemDetail.setProperty("invalid_parameters", errors);
        return problemDetail;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource tidak ditemukan pada URI: {} | Pesan: {}", request.getRequestURI(), ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Resource Not Found");
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleInternalServerError(Exception ex, HttpServletRequest request) {
        log.error("Terjadi internal server error pada URI: {} | Penyebab: ", request.getRequestURI(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Terjadi kesalahan internal pada sistem kami."
        );
        problemDetail.setTitle("Internal Server Error");
        return problemDetail;
    }
}
