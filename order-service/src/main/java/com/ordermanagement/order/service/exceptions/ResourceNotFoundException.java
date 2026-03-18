package com.ordermanagement.order.service.exceptions;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends OrderException {

    public ResourceNotFoundException(String message) {
        super(
                message,
                "Resource Not Found",
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND"
        );
    }
}