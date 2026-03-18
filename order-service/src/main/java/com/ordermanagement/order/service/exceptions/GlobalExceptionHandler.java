package com.ordermanagement.order.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                "httpStatus", status.toString(),
                "sourceApplication", SOURCE_APP
        );
    }


    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(AppException ex) {

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