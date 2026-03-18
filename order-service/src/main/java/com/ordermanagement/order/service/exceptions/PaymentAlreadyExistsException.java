package com.ordermanagement.order.service.exceptions;

import org.springframework.http.HttpStatus;

public class PaymentAlreadyExistsException extends OrderException {

    public PaymentAlreadyExistsException(String message) {
        super(
                message,
                "Payment Already Exists",
                HttpStatus.CONFLICT,
                "PAYMENT_ALREADY_EXISTS"
        );
    }
}