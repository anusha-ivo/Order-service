package com.ordermanagement.order.service.exceptions;


import org.springframework.http.HttpStatus;

public class SerializationException extends AppException {
    public SerializationException(String message) {
        super(message, "Serialization Error", HttpStatus.INTERNAL_SERVER_ERROR, "SERIALIZATION_ERROR");
    }
}