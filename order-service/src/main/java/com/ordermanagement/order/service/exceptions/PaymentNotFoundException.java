package com.ordermanagement.order.service.exceptions;

import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends AppException {

    public PaymentNotFoundException(String message) {
        super(
                message,
                "Payment Not Found",
                HttpStatus.NOT_FOUND,"PAYMENT_NOT_FOUND"
        );
    }
}
