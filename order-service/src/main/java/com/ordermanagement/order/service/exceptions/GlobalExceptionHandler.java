package com.ordermanagement.order.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String SOURCE_APP = "ORDER-SERVICE";

    private Map<String, Object> buildError(
            String label,
            String code,
            HttpStatus status,
            String message,
            String level,
            String severity) {

        return Map.of(
                "label", label,
                "code", code,
                "level", level,
                "severity", severity,
                "message", message,
                "httpStatus", status.name(),
                "sourceApplication", SOURCE_APP
        );
    }


    @ExceptionHandler(OrderException.class)
    public ResponseEntity<?> handleAppException(OrderException ex) {

        return new ResponseEntity<>(
                buildError(
                        ex.getLabel(),
                        ex.getErrorCode(),
                        ex.getStatus(),
                        ex.getMessage(),
                        "REQUEST",
                        "NONFATAL"
                ),
                ex.getStatus()
        );
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Validation failed for request");
        body.put("status", HttpStatus.BAD_REQUEST);
        body.put("label", "VALIDATION_ERROR");
        body.put("errors", errors);
        body.put("errorCode", "INVALID_FIELDS");

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ex.printStackTrace();

        return new ResponseEntity<>(
                buildError(
                        "Internal Server Error",
                        "INTERNAL_ERROR",
                        status,
                        "Something went wrong",
                        "SYSTEM",
                        "FATAL"
                ),
                status
        );
    }
}