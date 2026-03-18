package com.ordermanagement.order.service.exceptions;


import org.springframework.http.HttpStatus;

public class ExternalServiceException extends AppException {
    public ExternalServiceException(String message) {
        super(message, "External Service Error", HttpStatus.BAD_GATEWAY, "EXTERNAL_SERVICE_ERROR");
    }
}