package com.ordermanagement.order.service.exceptions;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException{
    private final String label;
    private final HttpStatus status;
    private final String errorCode;

    public AppException(String message, String label, HttpStatus status,String errorCode) {
        super(message);
        this.label = label;
        this.status = status;
        this.errorCode=errorCode;
    }

    public String getLabel() {
        return label;
    }

    public HttpStatus getStatus() {
        return status;
    }
    public String getErrorCode() {
        return errorCode;
    }

}
