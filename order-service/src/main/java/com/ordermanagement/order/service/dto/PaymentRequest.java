package com.ordermanagement.order.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request object to initiate a payment for an order")
public class PaymentRequest {

    @NotNull
    @Schema(description = "ID of the order to pay for", example = "1001", required = true)
    private Long orderId;

    @NotNull
    @Schema(description = "Payment amount", example = "250.75", required = true)
    private BigDecimal amount;

    @NotBlank
    @Schema(description = "Currency code for the payment (ISO 4217)", example = "USD", required = true)
    private String currency;

    @NotBlank
    @Schema(description = "Payment method (e.g., UPI, CARD, PAYPAL)", example = "UPI", required = true)
    private String method;

    @NotBlank
    @Schema(description = "Idempotency key to prevent duplicate payments", example = "ORDER_1001", required = true)
    private String idempotencyKey;
}