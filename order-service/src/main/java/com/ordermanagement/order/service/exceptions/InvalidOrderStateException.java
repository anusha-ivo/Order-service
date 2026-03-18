package com.ordermanagement.order.service.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidOrderStateException extends AppException {

    public InvalidOrderStateException(String message) {
        super(
                message,
                "Invalid Payment State",
                HttpStatus.BAD_REQUEST,
                "INVALID_PAYMENT_STATE"
        );
    }
}