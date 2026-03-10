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
            HttpStatus status,
            String message,
            String level,
            String severity) {

        return Map.of(
                "label", label,
                "code", String.valueOf(status.value()),
                "level", level,
                "severity", severity,
                "message", message,
                "httpStatus", status.toString(),
                "sourceApplication", SOURCE_APP
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {

        HttpStatus status = HttpStatus.NOT_FOUND;

        return new ResponseEntity<>(
                buildError("Not Found",
                        status,
                        ex.getMessage(),
                        "REQUEST",
                        "NONFATAL"),
                status
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleBadRequest(IllegalStateException ex) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(
                buildError("Bad Request",
                        status,
                        ex.getMessage(),
                        "REQUEST",
                        "NONFATAL"),
                status
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ex.printStackTrace();

        return new ResponseEntity<>(
                buildError("Internal Server Error",
                        status,
                        "Something went wrong",
                        "SYSTEM",
                        "FATAL"),
                status
        );
    }
}